package com.example.android.booklisting;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Helper methods related to requesting and receiving book data from DOUBAN.
 */
public class QueryUtils {

    /**
     * Tag for the log messages.
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Result count of the books fetch from Internet.
     */
    public static int resultCount;

    /**
     * ArrayList that save the image String of the books fetch from Internet.
     */
    public static List<String> image;

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the DOUBAN data set and return a list of {@link Book} objects.
     */
    public static List<Book> fetchBookData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTPS request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpsRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTPS request.", e);
        }

        // Extract relevant fields from the JSON response,
        // Create a list of {@link Book}s and return it.
        return extractFeatureFromJson(jsonResponse);
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTPS request to the given URL and return a String as the response.
     */
    private static String makeHttpsRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Book> extractFeatureFromJson(String bookJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        // Create an empty ArrayList that can start adding books to.
        // This is the only place assign {@link List} to {@link ArrayList}.
        List<Book> books = new ArrayList<>();

        // Create an empty ArrayList that can start adding image url string to.
        image = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // Create a JSONObject from the JSON response string.
            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            /**
             * Get the result count from the key called "total".
             */
            resultCount = baseJsonResponse.getInt("total");

            // Extract the JSONArray associated with the key called "books",
            // which represents a list of books.
            JSONArray bookArray = baseJsonResponse.getJSONArray("books");

            // For each book in the bookArray, create an {@link Book} object.
            for (int i = 0; i < bookArray.length(); i++) {

                // Get a single book at position i within the list of books.
                JSONObject currentBook = bookArray.getJSONObject(i);

                // For a given book, extract the String associated with the
                // key called "subtitle", which represents the subtitle of the book.
                String subtitle = currentBook.getString("subtitle");

                // For a given book, extract the String associated with the
                // key called "title", which represents the title of the book.
                String title = currentBook.getString("title");

                // For a given book, extract the JSONArray associated with the
                // key called "author", which represents a list of book author.
                JSONArray authorArray = currentBook.getJSONArray("author");
                // Create the book author string by default value null.
                String author = null;
                // If the authorArray has the book author value, set it to the string.
                if (authorArray != null && authorArray.length() > 0) {
                    // Extract the first value of the authorArray,
                    // which represents get the first author of the book.
                    author = authorArray.getString(0);
                }

                // For a given book, extract the String associated with the
                // key called "summary", which represents the summary of book.
                String summary = currentBook.getString("summary");

                // For a given book, extract the String associated with the
                // key called "summary", which represents the summary of book.
                String link = currentBook.getString("alt");

                // For a given book, extract the object associated with the
                // key called "rating", which represents the rating of the book.
                JSONObject rating = currentBook.getJSONObject("rating");

                // Extract the value for the key called "average",
                // which represents get the rate for the book.
                double rate = rating.getDouble("average");

                // Extract the value for the key called "max",
                // which represents get the max value of the rating.
                int maxRating = rating.getInt("max");

                /**
                 *  For a given book, extract the Object for the key called "images",
                 *  which represents a set of images of the book.
                 *  Then extract the value for the key called "large",
                 *  which represents get the large image of the book.
                 *  Finally, add the image String to the image ArrayList.
                 */
                image.add(currentBook.getJSONObject("images").getString("large"));

                // Create a new {@link Book} object with the subtitle, title, author, summary,
                // rate, maxRating from the JSON response.
                // Set the image resource to null for the time being.
                // Image resource will be set in the ImageLoader, after this Loader,
                // in order to get a fast loading speeding.
                Book book = new Book(subtitle, title, author, summary, link,
                        rate, maxRating, null);

                // Add the new {@link Book} to the list of books.
                books.add(book);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the book JSON results", e);
        }

        // Return the list of books.
        return books;
    }
}
