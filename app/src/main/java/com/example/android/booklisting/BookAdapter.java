package com.example.android.booklisting;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link BookAdapter} is an {@link RecyclerView.Adapter} that can provide the layout
 * for each list item based on a data source, which is a list of {@link Book} objects.
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.MyViewHolder> {

    /**
     * Create a new list of {@link Book} object.
     */
    private List<Book> mBooksList;

    /**
     * Context passed in through the constructor.
     */
    private Context mContext;

    /**
     * Create a new {@link BookAdapter} object.
     *
     * @param context   is the context of the Activity.
     * @param booksList is a list of {@link Book} objects。
     */
    public BookAdapter(Context context, List<Book> booksList) {
        mContext = context;
        mBooksList = booksList;
    }

    /**
     * Create a RecyclerView OnItemClickListener object.
     */
    private OnItemClickListener mOnItemClickListener;

    /**
     * Setup the RecyclerView item click listener.
     *
     * @param OnItemClickListener is the interface of RecyclerVIew OnItemClickListener.
     */
    public void setOnItemClickListener(OnItemClickListener OnItemClickListener) {
        mOnItemClickListener = OnItemClickListener;
    }

    /**
     * The interface of RecyclerVIew OnItemClickListener.
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * Called when {@link RecyclerView} needs a new {@link RecyclerView.ViewHolder}
     * of the given type to represent an item.
     * Usually involves inflating a layout from XML and returning the holder.
     *
     * @param parent   is the ViewGroup into which the new View will be added
     *                 after it is bound to an adapter position.
     * @param viewType is the view type of the new View.
     * @return a new ViewHolder that holds a View of the given view type.
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the custom layout.
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        // Return a new holder instance.
        return new MyViewHolder(itemView);
    }

    /**
     * List that saves the status of whether CardView is expanded of not.
     */
    private List<Boolean> isCardExpanded = new ArrayList<>();

    /**
     * Get the size of the list of {@link Book} object.
     * Must override this method.
     *
     * @return the size of the list of {@link Book} object.
     */
    @Override
    public int getItemCount() {
        int listItemCount = mBooksList.size();
        // Make sure that the CardView indicator list size will not larger than the book list one.
        if (isCardExpanded.size() < listItemCount) {
            // Clear the list before add more item to it.
            isCardExpanded.clear();
            // Add CardView expand status according to list item count.
            for (int index = 0; index < listItemCount; index++) {
                isCardExpanded.add(false);
            }
        }
        return listItemCount;
    }

    /**
     * CardView index assign by the item click listener.
     * Default value is -1, so that it can't be reach until item click event assign a new value.
     */
    private int cardViewIndex = -1;

    /**
     * Involves populating data into the item through holder.
     *
     * @param holder   is the custom ViewHolder.
     * @param position is the current position in RecyclerView.
     */
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        // Get the data model based on position.
        final Book book = mBooksList.get(position);

        // Get the origin vertical and horizontal margin of CardView.
        int originVerticalMargin = mContext.getResources().
                getDimensionPixelOffset(R.dimen.card_vertical_margin);
        int originHorizontalMargin = mContext.getResources().
                getDimensionPixelOffset(R.dimen.card_horizontal_margin);
        // Use the helper method to set CardView Margins.
        setMargins(holder.cardView, originHorizontalMargin, originVerticalMargin,
                originHorizontalMargin, originVerticalMargin);

        // Set the subtitle of the book to the TextView.
        holder.bookSubtitleView.setText(book.getSubtitle());
        // Set the title of the book to the TextView.
        holder.bookTitleView.setText(book.getTitle());
        // Set the author of the book to the TextView.
        holder.bookAuthorView.setText(book.getAuthor());
        // Set the summary of the book to gone by default.
        holder.bookSummaryView.setVisibility(View.GONE);
        // Set the link of the book to gone by default.
        holder.bookLinkView.setVisibility(View.GONE);

        // If the rate for the book is zero, which means there is no rating for the book.
        // Otherwise, set the rate for the book to the TextView.
        if (book.getRate() == 0) {
            holder.bookRateView.setText(R.string.no_rating);
        } else {
            holder.bookRateView.setText(mContext.getString(R.string.rating_string,
                    book.getRate(), book.getMaxRating()));
        }

        // If the image resource is not null. then set the image of the book to the ImageView.
        if (book.getImageResource() != null) {
            holder.bookImageView.setImageDrawable(book.getImageResource());
        }

        // Create an instance of RecyclerVIew OnItemClickListener.
        if (mOnItemClickListener != null) {
            // CardView click listener.
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Implement the onItemClick method in MainActivity.
                    mOnItemClickListener.onItemClick(view, holder.getAdapterPosition());

                    // Get the position of CardView whom needs to expand.
                    cardViewIndex = holder.getAdapterPosition();
                    // Notify the adapter of the item change.
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });

            // Book link TextView click listener.
            holder.bookLinkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Implement the onItemClick method in MainActivity.
                    mOnItemClickListener.onItemClick(view, holder.getAdapterPosition());

                    // Intent to browser according to the book link.
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(book.getLink()));
                    mContext.startActivity(intent);
                }
            });
        }

        // Expand CardView and adjust other views here.
        if (cardViewIndex == position) {
            // Create a LayoutParams object for CardView.
            ViewGroup.LayoutParams cardViewLayoutParams = holder.cardView.getLayoutParams();
            // Create a LayoutParams object for the book image.
            ViewGroup.LayoutParams imageLayoutParams = holder.bookImageView.getLayoutParams();

            // If the card is not expanded, proceed the expanding.
            // If the card has already expanded, restore the origin layout.
            if (isCardExpanded.get(position).equals(false)) {
                // Make subtitle and author view gone.
                holder.bookSubtitleView.setVisibility(View.GONE);
                holder.bookAuthorView.setVisibility(View.GONE);
                // Set the text for book link TextView and make it visible.
                holder.bookLinkView.setText(R.string.learn_more);
                holder.bookLinkView.setVisibility(View.VISIBLE);
                // Set the text for book summary TextView and make it visible.
                holder.bookSummaryView.setText(book.getSummary());
                holder.bookSummaryView.setVisibility(View.VISIBLE);
                // Expand the CardView height and width.
                int expandedHorizontalMargin = mContext.getResources().
                        getDimensionPixelOffset(R.dimen.card_expanded_horizontal_margin);
                int expandedVerticalMargin = mContext.getResources().
                        getDimensionPixelOffset(R.dimen.card_expanded_vertical_margin);
                setMargins(holder.cardView, expandedHorizontalMargin, expandedVerticalMargin,
                        expandedHorizontalMargin, expandedVerticalMargin);
                cardViewLayoutParams.height = (int) mContext.getResources().
                        getDimension(R.dimen.card_expanded_height);

                // Expand the book image width.
                imageLayoutParams.width = (int) mContext.getResources().
                        getDimension(R.dimen.book_image_expanded_width);

                // Set the corresponding position of CardView expend status to true.
                isCardExpanded.set(position, true);
            } else {
                // Restore the origin layout.
                holder.bookSubtitleView.setVisibility(View.VISIBLE);
                holder.bookAuthorView.setVisibility(View.VISIBLE);
                cardViewLayoutParams.height = (int) mContext.getResources().
                        getDimension(R.dimen.card_height);
                imageLayoutParams.width = (int) mContext.getResources().
                        getDimension(R.dimen.book_image_width);

                // Set the corresponding position of CardView expend status to false.
                isCardExpanded.set(position, false);
            }

            // Set the layout params to the views.
            holder.cardView.setLayoutParams(cardViewLayoutParams);
            holder.bookImageView.setLayoutParams(imageLayoutParams);

            // After dealing with the CardView, set the indicator value to -1, make it inaccessible.
            cardViewIndex = -1;
        }
    }

    /**
     * Provide a direct reference to each of the views within a data item.
     * Used to cache the views within the item layout for fast access.
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        // The holder should contain a member variable
        // for any view that will be set as you render a row.
        public CardView cardView;
        public TextView bookSubtitleView, bookTitleView, bookAuthorView,
                bookSummaryView, bookLinkView, bookRateView;
        public ImageView bookImageView;

        // Create a constructor that accepts the entire item row
        // and does the view lookups to find each subview.
        public MyViewHolder(View view) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(view);

            cardView = view.findViewById(R.id.card_view);
            bookSubtitleView = view.findViewById(R.id.book_subtitle);
            bookTitleView = view.findViewById(R.id.book_title);
            bookAuthorView = view.findViewById(R.id.book_author);
            bookSummaryView = view.findViewById(R.id.book_summary);
            bookLinkView = view.findViewById(R.id.book_link);
            bookRateView = view.findViewById(R.id.book_rate);
            bookImageView = view.findViewById(R.id.book_image);
        }

    }

    /**
     * Helper method that clear the list of {@link RecyclerView} and notify it of the removal.
     */
    public void clear() {
        mBooksList.clear();
        notifyDataSetChanged();
    }

    /**
     * Helper method that pass in the list of {@link RecyclerView} and notify it of the data change.
     *
     * @param books is a reference of the {@link List<Book>}.
     */
    public void addAll(List<Book> books) {
        mBooksList.addAll(books);
        notifyDataSetChanged();
    }

    /**
     * Helper method that set images of books and notify {@link RecyclerView} of the item change.
     *
     * @param drawables is a list of the drawable resource of the image.
     */
    public void setImage(List<Drawable> drawables) {
        if (drawables != null && !drawables.isEmpty()) {
            for (int index = 0; index < drawables.size(); index++) {
                mBooksList.get(index).setImageResource(drawables.get(index));
                notifyItemChanged(index);
            }
        }
    }

    /**
     * Helper method that set margins of views, using {@link ViewGroup.MarginLayoutParams}.
     *
     * @param view         is the view whom set margins to.
     * @param leftMargin   is the left margin of the view.
     * @param topMargin    is the top margin of the view.
     * @param rightMargin  is the right margin of the view.
     * @param bottomMargin is the bottom margin of the view.
     */
    private void setMargins(View view, int leftMargin, int topMargin,
                            int rightMargin, int bottomMargin) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
            view.requestLayout();
        }
    }
}
