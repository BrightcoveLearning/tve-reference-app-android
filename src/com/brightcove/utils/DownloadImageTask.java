package com.brightcove.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

/**
 * Utility AsyncTask class to managing the loading of an image into a {@link android.widget.ImageView} or as a background to a {@link android.view.View}.
 * <p>
 * Example:<br>
 * {@code new DownloadImageTask(view).execute(imageUrl, fallbackImageUrl, ...); }
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @see android.os.AsyncTask
 * @since 1.0
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    // The view element to load the image for
    private final WeakReference<View> viewReference;

    /**
     * Construct a new DownloadImageTask for loading and displaying an image from an Url
     * @param view the view element to load the image for
     * @since 1.0
     */
    public DownloadImageTask(View view) {
        viewReference = new WeakReference<View>(view);
    }

    /**
     * Background process for loading the bitmap image, and attempt to load fallback images if necessary
     * @param urls string array of image url, starting with the preferred image url followed by any number of fallback images
     * @return the loaded bitmap image to use for updating the view element
     * @since 1.0
     */
    @Override
    protected Bitmap doInBackground(String... urls) {
        Bitmap bitmap = null;
        for( String url : urls ) {
            try {
                bitmap = getBitmapImage(url);
                break;
            }
            catch( Exception e ) {
                Log.d( "[DownloadImageTask#doInBackground]", "Unable to load image from url '" + url + "', attempts to load the next fallback image");
            }
        }
        return bitmap;
    }

    /**
     * Updates the view element with the loaded image. This part happens in the main UI thread
     * @param bitmap the loaded bitmap image to use for updating the view element
     * @since 1.0
     */
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        final View view = viewReference.get();
        if( view != null ) {
            if( view instanceof ImageView ) {
                ((ImageView) view).setImageBitmap(bitmap);
            }
            else {
                BitmapDrawable background = null;
                if( bitmap != null ) {
                    background = new BitmapDrawable(null, bitmap);
                }
                view.setBackgroundDrawable(background);
            }
        }
    }

    /**
     * The actual loading of the image url
     * @param url the image url to load the image from
     * @return the loaded bitmap image to use for updating the view element
     * @throws Exception if the loading fails for any reason
     */
    private Bitmap getBitmapImage(String url) throws Exception {
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            InputStream in = urlConnection.getInputStream();
            try {
                bitmap = BitmapFactory.decodeStream(in);
            }
            catch (Exception e) {
                Log.d( "[DownloadImageTask#getBitmapImage]", "Failed extracting bitmap from the url '" + url + "'");
                throw e;
            }
            finally {
                in.close();
            }
        }
        catch( SocketTimeoutException ste ) {
            Log.e("[DownloadImageTask#getBitmapImage]", "Timeout loading: '" + url + "'");
            throw ste;
        }
        catch (Exception e) {
            Log.e("[DownloadImageTask#getBitmapImage]", e.getMessage());
            e.printStackTrace();
            throw e;
        }
        finally {
            if( urlConnection != null ) {
                urlConnection.disconnect();
            }
        }
        return bitmap;
    }

}
