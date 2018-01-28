package com.example.android.booklisting;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * URL for book data from the DOUBAN data set.
     */
    private static final String DOUBAN_REQUEST_URL = "https://api.douban.com/v2/book/search";

    /**
     * Constant value for the number of books per request.
     */
    private static final int NUMBER_PER_REQUEST = 10;

    /**
     * Result offset for each request.
     */
    private int resultOffset = 0;

    /**
     * Request keywords fetch from users input.
     */
    private String requestKeywords = null;

    /**
     * Constant value for the book loader ID, which can be any integer.
     * There are two ID, one for book, the other for image.
     */
    private static final int BOOK_LOADER_ID = 1;
    private static final int IMAGE_LOADER_ID = 2;

    /**
     * {@link LoaderManager} for the both {@link BookLoader} and {@link ImageLoader}.
     */
    private LoaderManager loaderManager;

    /**
     * Pull to refresh layout.
     */
    private SwipeRefreshLayout swipeContainer;

    /**
     * {@link TextView} that display the status of the list.
     */
    private TextView mEmptyStateView,
            resultPageView, resultCountView,
            bottomLeftView, bottomRightView;

    /**
     * Adapter for the list of books.
     */
    private BookAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the reference to each view.
        resultCountView = findViewById(R.id.result_count);
        resultPageView = findViewById(R.id.result_page);
        bottomLeftView = findViewById(R.id.list_bottom_left);
        bottomRightView = findViewById(R.id.list_bottom_right);
        mEmptyStateView = findViewById(R.id.empty_view);

        // Set up a onclick listener for empty view to open search view.
        mEmptyStateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Expand search view.
                searchMenuItem.expandActionView();
                searchView.setIconified(false);
            }
        });

        // Find a reference to the {@link RecyclerView} in the layout.
        RecyclerView recyclerView = findViewById(R.id.list);
        // Create an {@link BookAdapter}, whose data source is a list of {@link Book}s.
        // The adapter knows how to create list items for each item in the list.
        mAdapter = new BookAdapter(this, new ArrayList<Book>());
        // Setup the DefaultItemAnimator for the ItemAnimator of RecyclerView.
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // Setup the LinearLayoutManager for the LayoutManager of RecyclerView.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Set nested scroll to false to get smooth scroll of RecyclerView inside NestedScrollView.
        recyclerView.setNestedScrollingEnabled(false);
        // Make the {@link RecycleView} use the {@link BookAdapter} created above, so that the
        // {@link RecycleView} will display list items for each {@link Book} in the list.
        recyclerView.setAdapter(mAdapter);
        // Setup an OnItemClickListener to handle the click event of the RecyclerView item.
        mAdapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            }
        });

        if (savedInstanceState != null) {
            // Restore the saved variables.
            resultOffset = savedInstanceState.getInt("resultOffset");
            requestKeywords = savedInstanceState.getString("requestKeywords");
            // When request keywords is not null, restore the list.
            if (requestKeywords != null) {
                // Get a reference to the LoaderManager, in order to interact with loaders.
                loaderManager = getLoaderManager();
                // Restart the book loader.
                loaderManager.restartLoader(BOOK_LOADER_ID, null, new BookLoaderCallback());
            }
        }

        if (!isConnected()) {
            // If there is no internet connection, display error.
            // Update empty state with no connection error message.
            setEmptyStateView(R.string.no_internet_connection, R.drawable.no_connection);
        } else if (requestKeywords == null) {
            // If users did not input any keywords, display hint.
            // Update empty state with hint message.
            setEmptyStateView(R.string.search_text, R.drawable.search);
        }

        // Lookup the swipe container view.
        swipeContainer = findViewById(R.id.swipe_container);
        // Setup refresh listener which triggers new data loading.
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Only when there is internet connection and not empty list, fresh the data.
                if (!isConnected()) {
                    // Call setRefreshing(false) to signal refresh has finished.
                    swipeContainer.setRefreshing(false);
                    // Make a toast to inform users that the device is disconnected.
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getString(R.string.no_internet_connection), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else if (mAdapter.getItemCount() == 0) {
                    // Call setRefreshing(false) to signal refresh has finished.
                    swipeContainer.setRefreshing(false);
                } else {
                    // Get a reference to the LoaderManager, in order to interact with loaders.
                    loaderManager = getLoaderManager();
                    // Restart the book loader.
                    loaderManager.restartLoader(BOOK_LOADER_ID, null, new BookLoaderCallback());
                }
            }
        });
        // Configure the refreshing colors.
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Find the list bottom LinearLayout and set the swipe touch listener to it.
        LinearLayout listBottom = findViewById(R.id.list_bottom);
        // Android Studio thinks this LinearLayout is a custom view,
        // and did not override perform click method, therefore the warning below.
        listBottom.setOnTouchListener(new OnSwipeTouchListener(this) {
            // When users swipe right, fetch last ten books from web api.
            public void onSwipeRight() {
                if (!isConnected()) {
                    // Set text to list bottom TextView when there is no internet connection.
                    bottomLeftView.setText(R.string.no_internet_connection);
                    bottomRightView.setText(R.string.no_internet_connection);
                } else if (resultOffset != 0) {
                    // Set refreshing to true because it begin to fetch new data.
                    swipeContainer.setRefreshing(true);
                    // Set the background as the touch feedback.
                    bottomLeftView.setBackgroundResource(R.drawable.gradient_blue_right);
                    // Fetch data until the list reach the start.
                    resultOffset -= NUMBER_PER_REQUEST;
                    loaderManager.restartLoader(BOOK_LOADER_ID, null, new BookLoaderCallback());
                }
            }

            // When users swipe left, fetch next ten books from web api.
            public void onSwipeLeft() {
                if (!isConnected()) {
                    // Set text to list bottom TextView when there is no internet connection.
                    bottomLeftView.setText(R.string.no_internet_connection);
                    bottomRightView.setText(R.string.no_internet_connection);
                } else if (resultOffset == 0 ||
                        resultOffset <= QueryUtils.resultCount - NUMBER_PER_REQUEST) {
                    // Set refreshing to true because it begin to fetch new data.
                    swipeContainer.setRefreshing(true);
                    // Set the background as the touch feedback.
                    bottomRightView.setBackgroundResource(R.drawable.gradient_blue_left);
                    // Fetch data until the list reach the end.
                    resultOffset += NUMBER_PER_REQUEST;
                    loaderManager.restartLoader(BOOK_LOADER_ID, null, new BookLoaderCallback());
                }
            }
        });
    }

    // Save the needed variable state,
    // when phone rotate to the landscape mode or portrait mode.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putInt("resultOffset", resultOffset);
        savedInstanceState.putString("requestKeywords", requestKeywords);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * This inner class is for {@link BookLoader},
     * which implements its {@link LoaderManager.LoaderCallbacks}.
     */
    private class BookLoaderCallback implements LoaderManager.LoaderCallbacks<List<Book>> {
        /**
         * This method will be called when it needs to create a new {@link Loader}.
         *
         * @param i      is the ID whose loader is to be created.
         * @param bundle is any arguments supplied by the caller. Here is null.
         * @return a new custom AsyncTaskLoader.
         */
        @Override
        public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {

            // Use {@link Uri.Builder} to build a request url.
            Uri baseUri = Uri.parse(DOUBAN_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendQueryParameter("q", requestKeywords);
            uriBuilder.appendQueryParameter("start", Integer.toString(resultOffset));
            uriBuilder.appendQueryParameter("count", Integer.toString(NUMBER_PER_REQUEST));

            return new BookLoader(getApplicationContext(), uriBuilder.toString());
        }

        /**
         * This method will be called when the {@link Loader} finish loading in the working thread.
         *
         * @param loader is an instance of the {@link Loader}.
         * @param books  is the result of the loading in the working thread.
         */
        @Override
        public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {
            // Clear the adapter of previous book data.
            mAdapter.clear();

            // Call setRefreshing(false) to signal refresh has finished.
            swipeContainer.setRefreshing(false);

            // If there is a valid list of {@link Book}s, then add them to the adapter's data set.
            if (books != null && !books.isEmpty()) {
                // Add the list of book through adapter.
                mAdapter.addAll(books);

                // Scroll to the top after refreshing data.
                NestedScrollView scrollView = findViewById(R.id.scroll_view);
                scrollView.scrollTo(0, 0);

                // Hide empty state view.
                mEmptyStateView.setVisibility(View.GONE);
                // Set the result count to the TextView.
                setResultCount();
                // Set the result page to the TextView.
                setResultPage();
                // Set the bottom line text to the TextView.
                setBottomLineText();

                // Starts a new or restarts an existing Loader of book images.
                loaderManager.restartLoader(IMAGE_LOADER_ID, null, new ImageLoaderCallback());
            } else if (!isConnected()) {
                // Set no internet connection empty state.
                setEmptyStateView(R.string.no_internet_connection, R.drawable.no_connection);
            } else {
                // Set no book found empty state.
                setEmptyStateView(R.string.no_book, R.drawable.no_book);
                // Set other views to gone.
                resultCountView.setVisibility(View.GONE);
                resultPageView.setVisibility(View.GONE);
                bottomLeftView.setVisibility(View.GONE);
                bottomRightView.setVisibility(View.GONE);
            }
        }

        /**
         * This method will be called when the {@link Loader} reset.
         *
         * @param loader is an instance of the {@link Loader}.
         */
        @Override
        public void onLoaderReset(Loader<List<Book>> loader) {
            // Loader reset, clear out our existing data.
            mAdapter.clear();
        }
    }

    /**
     * This inner class is for {@link ImageLoader},
     * which implements its {@link LoaderManager.LoaderCallbacks}.
     */
    private class ImageLoaderCallback implements LoaderManager.LoaderCallbacks<List<Drawable>> {
        /**
         * This method will be called when it needs to create a new {@link Loader}.
         *
         * @param i      is the ID whose loader is to be created.
         * @param bundle is any arguments supplied by the caller. Here is null.
         * @return a new custom AsyncTaskLoader.
         */
        @Override
        public Loader<List<Drawable>> onCreateLoader(int i, Bundle bundle) {
            return new ImageLoader(getApplicationContext());
        }

        /**
         * This method will be called when the {@link Loader} finish loading in the working thread.
         *
         * @param loader    is an instance of the {@link Loader}.
         * @param drawables is the result of the loading in the working thread.
         */
        @Override
        public void onLoadFinished(Loader<List<Drawable>> loader, List<Drawable> drawables) {
            // Set these two variables here is because it can use the updated result offset.
            // Such as the device rotate from landscape mode to portrait mode.
            // Set the result page to the TextView.
            setResultPage();
            // Set the bottom line text to the TextView.
            setBottomLineText();

            if (drawables != null && !drawables.isEmpty()) {
                // Set the drawable resource to the ImageView through adapter.
                mAdapter.setImage(drawables);
            }
        }

        /**
         * This method will be called when the {@link Loader} reset.
         *
         * @param loader is an instance of the {@link Loader}.
         */
        @Override
        public void onLoaderReset(Loader<List<Drawable>> loader) {
            // Loader reset, clear out our existing data.
            mAdapter.clear();
        }
    }

    /**
     * Search menu item in options_menu.xml.
     */
    MenuItem searchMenuItem;

    /**
     * {@link SearchView} widget in app bar.
     */
    SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML.
        getMenuInflater().inflate(R.menu.options_menu, menu);

        // Get the search view.
        searchMenuItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        // Set the hint text for the search view.
        searchView.setQueryHint(getString(R.string.search_hint));
        // Iconify the widget by default.
        searchView.setIconifiedByDefault(false);
        // Set up the query text listener, so that get the query that users input.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Only when there is internet connection, search data.
                if (isConnected()) {
                    // Set refreshing to true because it begin to fetch new data.
                    swipeContainer.setRefreshing(true);
                    // Set the query that users input to request keywords.
                    requestKeywords = query;
                    // Reset the result offset since this is a new request.
                    resultOffset = 0;
                    // Get a reference to the LoaderManager, in order to interact with loaders.
                    loaderManager = getLoaderManager();
                    // Restart the book loader.
                    loaderManager.restartLoader(BOOK_LOADER_ID, null, new BookLoaderCallback());
                } else if (mAdapter.getItemCount() == 0) {
                    setEmptyStateView(R.string.no_internet_connection, R.drawable.no_connection);
                } else {
                    // To maintain contents that users already get, only make a toast to
                    // notice users that the device is disconnected.
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getString(R.string.no_internet_connection), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    /**
     * Helper method that tells whether the device is connect to internet or not.
     *
     * @return true when the device is connected, false when it is not.
     */
    private boolean isConnected() {
        // Get a reference to the ConnectivityManager to check state of network connectivity.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network.
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // Return true if the device is connected, vice versa.
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Helper method that set the result count to the TextView.
     * The data is from {@link QueryUtils} class.
     */
    private void setResultCount() {
        if (QueryUtils.resultCount == 1) {
            resultCountView.setText(R.string.result_count_one);
        } else {
            resultCountView.setText(getString(R.string.result_count_string, QueryUtils.resultCount));
        }
        resultCountView.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method that set the result page to the TextView.
     */
    private void setResultPage() {
        // Calculate the current page according to the result offset.
        int currentPage = resultOffset / 10 + 1;
        // Calculate the current page according to {@link QueryUtils.resultCount}.
        int totalPage;
        if (QueryUtils.resultCount % 10 == 0) {
            totalPage = QueryUtils.resultCount / 10;
        } else {
            totalPage = QueryUtils.resultCount / 10 + 1;
        }
        resultPageView.setText(getString(R.string.result_page_string, currentPage, totalPage));
        resultPageView.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method that set the bottom line text to the TextView,
     * which help users to navigate through each page of book list.
     */
    private void setBottomLineText() {
        // Only when there are more than ten results, then display the TextView.
        if (QueryUtils.resultCount > 10) {
            // Set the two TextView to visible by default.
            bottomLeftView.setVisibility(View.VISIBLE);
            bottomRightView.setVisibility(View.VISIBLE);
            // If current page is the first page, do not show the left text.
            // If current page is the last page, do not show the right text.
            if (resultOffset / 10 == 0) {
                bottomLeftView.setVisibility(View.INVISIBLE);
            } else if (resultOffset >= QueryUtils.resultCount - NUMBER_PER_REQUEST) {
                bottomRightView.setVisibility(View.INVISIBLE);
            }

            // Set the text and drawable to the two TextView,
            // and set the background to transparent.
            bottomLeftView.setText(R.string.list_bottom_left);
            bottomLeftView.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    ContextCompat.getDrawable(this, R.drawable.arrow_right), null);
            bottomLeftView.setBackgroundColor(0);
            bottomRightView.setText(R.string.list_bottom_right);
            bottomRightView.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.arrow_left),
                    null, null, null);
            bottomRightView.setBackgroundColor(0);
        } else {
            // Set the two TextView to gone when there are less than ten results.
            bottomLeftView.setVisibility(View.GONE);
            bottomRightView.setVisibility(View.GONE);
        }
    }

    /**
     * Helper method that set the empty state to the TextView,
     * which tells users the current status of the app.
     *
     * @param textStringId    is the text string id of the TextView.
     * @param imageDrawableId is the compound image id of the TextView.
     */
    private void setEmptyStateView(int textStringId, int imageDrawableId) {
        mEmptyStateView.setText(textStringId);
        mEmptyStateView.setCompoundDrawablesWithIntrinsicBounds(null,
                ContextCompat.getDrawable(getApplicationContext(), imageDrawableId),
                null, null);
        mEmptyStateView.setCompoundDrawablePadding(getResources().
                getDimensionPixelOffset(R.dimen.compound_image_padding));
        mEmptyStateView.setVisibility(View.VISIBLE);
    }
}
