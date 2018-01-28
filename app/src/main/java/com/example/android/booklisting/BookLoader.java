package com.example.android.booklisting;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Loads a list of books by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class BookLoader extends AsyncTaskLoader<List<Book>> {

    /**
     * Query URL.
     */
    private String mUrl;

    /**
     * Constructs a new {@link BookLoader}.
     *
     * @param context of the activity.
     * @param url     to load data from.
     */
    public BookLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    /**
     * This method gets called automatically by initLoader method.
     * It should invoke forceLoad() method to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Book> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, extract a list of books, and return.
        return QueryUtils.fetchBookData(mUrl);
    }
}
