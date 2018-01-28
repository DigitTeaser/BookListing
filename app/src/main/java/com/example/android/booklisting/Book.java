package com.example.android.booklisting;

import android.graphics.drawable.Drawable;

/**
 * A {@link Book} object contains information related to a book.
 * It includes that an image, title, subtitle, author, summary and rate.
 */
public class Book {

    /**
     * Subtitle of the book.
     */
    private String mSubtitle;

    /**
     * Title of the book.
     */
    private String mTitle;

    /**
     * Author of the book.
     */
    private String mAuthor;

    /**
     * Summary of the book.
     */
    private String mSummary;

    /**
     * Link of the book.
     */
    private String mLink;

    /**
     * Rate for the book.
     */
    private double mRate;

    /**
     * Max rating for the book.
     */
    private int mMaxRating;

    /**
     * Image resource for the book.
     */
    private Drawable mImageResource;

    /**
     * Create a new Book object.
     *
     * @param subtitle      is the subtitle of the book.
     * @param title         is the title of the book.
     * @param author        is the author of the book.
     * @param summary       is the summary of the book.
     * @param link          is the link of the book.
     * @param rate          is the rate for the book.
     * @param maxRating     the max rating for the book.
     * @param imageResource is the image resource for the image associated with the book.
     */
    public Book(String subtitle, String title, String author, String summary, String link,
                double rate, int maxRating, Drawable imageResource) {
        mSubtitle = subtitle;
        mTitle = title;
        mAuthor = author;
        mSummary = summary;
        mLink = link;
        mRate = rate;
        mMaxRating = maxRating;
        mImageResource = imageResource;
    }

    /**
     * Return the subtitle of the book.
     */
    public String getSubtitle() {
        return mSubtitle;
    }

    /**
     * Return the title of the book.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Return the author of the book.
     */
    public String getAuthor() {
        return mAuthor;
    }

    /**
     * Return the link of the book.
     */
    public String getLink() {
        return mLink;
    }

    /**
     * Return the summary of the book.
     */
    public String getSummary() {
        return mSummary;
    }

    /**
     * Return the rate for the book.
     */
    public double getRate() {
        return mRate;
    }

    /**
     * Return the max rating for the book.
     */
    public int getMaxRating() {
        return mMaxRating;
    }

    /**
     * Return the image resource for the book.
     */
    public Drawable getImageResource() {
        return mImageResource;
    }

    /**
     * Set the image resource for the book.
     */
    public void setImageResource(Drawable imageResource) {
        mImageResource = imageResource;
    }
}
