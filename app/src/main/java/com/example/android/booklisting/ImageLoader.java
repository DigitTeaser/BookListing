package com.example.android.booklisting;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the image resource of books by using an AsyncTask to load drawable by the given URL.
 */
public class ImageLoader extends AsyncTaskLoader<List<Drawable>> {

    /**
     * Tag for the log messages.
     */
    private static final String LOG_TAG = ImageLoader.class.getSimpleName();

    /**
     * Constructs a new {@link BookLoader}.
     *
     * @param context of the activity.
     */
    public ImageLoader(Context context) {
        super(context);
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
    public List<Drawable> loadInBackground() {

        // Create an empty ArrayList that can start adding Drawable to.
        List<Drawable> drawables = new ArrayList<>();
        // Get a reference of the QueryUtils.image ArrayList .
        List<String> image = QueryUtils.image;

        if (image != null && !image.isEmpty()) {
            // Add Drawable resources using getImageDrawable method.
            for (int index = 0; index < image.size(); index++) {
                drawables.add(getImageDrawable(image.get(index)));
            }
        }

        // Return a list of Drawable.
        return drawables;
    }

    /**
     * Helper method that transfer the image url string to the drawable resource.
     *
     * @param imageUrlString is the image url string fetch from Internet.
     * @return imageResource is the image drawable resource.
     */
    private static Drawable getImageDrawable(String imageUrlString) {

        // Create a null drawable object.
        Drawable imageResource = null;

        // Use URL and InputStream class to get stream content.
        // And use createFromStream method to transfer stream to drawable.
        try {
            URL url = new URL(imageUrlString);
            InputStream content = (InputStream) url.getContent();
            imageResource = Drawable.createFromStream(content, "src");
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem getting the URL content ", e);
        }

        // Return the image drawable resource.
        return imageResource;
    }
}
