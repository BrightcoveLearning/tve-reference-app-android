package com.brightcove.auth.ap.view;

import android.content.Context;
import android.graphics.*;
import android.net.http.SslError;
import android.util.AttributeSet;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.adobe.adobepass.accessenabler.api.AccessEnabler;
import com.adobe.adobepass.accessenabler.utils.Log;
import com.brightcove.auth.ap.delegates.AdobePassDelegate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * WebView specifically designed to handle the Adobe Pass MVPD login.
 * When the MVPD login is complete it will automatically call the
 * {@link com.brightcove.auth.ap.delegates.AdobePassDelegate#finalizeAuthentication()}
 * <p>
 * Initiate and load as the standard {@link android.webkit.WebView},
 * but make sure to register the close listener by calling {@link com.brightcove.auth.ap.view.AdobePassLoginView#setCloseListener(CloseListener)}
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @see com.brightcove.auth.ap.delegates.AdobePassDelegate#finalizeAuthentication()
 * @see CloseListener
 * @see android.webkit.WebView
 * @since 1.0
 */
public class AdobePassLoginView extends WebView {
    private static final String LOG_TAG = "[AdobePassLoginView]";
    private CloseListener closeListener;

    /**
     * Construct a new AdobePassLoginView with a Context object.
     * @param context A Context object used to access application assets.
     * @since 1.0
     */
    public AdobePassLoginView(Context context) {
        super(context);
        construct();
    }
    /**
     * Construct a new AdobePassLoginView with layout parameters.
     * @param context A Context object used to access application assets.
     * @param attrs An AttributeSet passed to our parent.
     * @since 1.0
     */
    public AdobePassLoginView(Context context, AttributeSet attrs) {
        super(context,attrs);
        construct();
    }
    /**
     * Construct a new AdobePassLoginView with layout parameters and a default style.
     * @param context A Context object used to access application assets.
     * @param attrs An AttributeSet passed to our parent.
     * @param defStyle The default style resource ID.
     * @since 1.0
     */
    public AdobePassLoginView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        construct();
    }
    /**
     * Construct a new AdobePassLoginView with layout parameters and a default style.
     * @param context A Context object used to access application assets.
     * @param attrs An AttributeSet passed to our parent.
     * @param defStyle The default style resource ID.
     * @since 1.0
     */
    public AdobePassLoginView(Context context, AttributeSet attrs, int defStyle, boolean privateBrowsing) {
        super(context,attrs,defStyle,privateBrowsing);
        construct();
    }

    /**
     * [INTERNAL] Configures the WebViewClient
     */
    private void construct() {
        // Do not process for the Graphical Layout
        if( !isInEditMode() ) {
            // Configures the WebViewClient
            WebSettings browserSettings = getSettings();
            browserSettings.setJavaScriptEnabled(true);
            browserSettings.setDomStorageEnabled(true);
            browserSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            setWebViewClient(webViewClient);
        }
    }

    /**
     * Assigns the {@link CloseListener} which {@code onClose()} method will be called when the MVPD login is complete.
     *
     * @param closeListener CloseListener which onClose() method will be called when login is complete
     * @see CloseListener#onClose()
     * @since 1.0
     */
    public void setCloseListener(CloseListener closeListener) {
        this.closeListener = closeListener;
    }

    /**
     * [INTERNAL] Defines the WebViewClient.
     * Detects when the AuthN process is completed by checking the loading url
     * and comparing it with static variable {@link com.adobe.adobepass.accessenabler.api.AccessEnabler#ADOBEPASS_REDIRECT_URL}
     * Once AuthN compete is detected {@link com.brightcove.auth.ap.delegates.AdobePassDelegate#finalizeAuthentication()} will be called
     * followed by {@link CloseListener#onClose()}
     *
     * @see com.brightcove.auth.ap.delegates.AdobePassDelegate#finalizeAuthentication()
     * @see CloseListener
     * @since 1.0
     */
    private final WebViewClient webViewClient = new WebViewClient() {
        private static final String LOG_TAG = "[AdobePassLoginView/webViewClient]";

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(LOG_TAG, "Loading URL: " + url);

            // If detecting a redirect to AdobePass "fake" REDIRECT URL
            // then the AuthN flow is completed
            try {
                if (url.equals(URLDecoder.decode(AccessEnabler.ADOBEPASS_REDIRECT_URL, "UTF-8"))) {
                    ((AdobePassDelegate)AdobePassDelegate.getInstance()).finalizeAuthentication();
                    closeListener.onClose();
                    return true;
                }
            }
            catch( UnsupportedEncodingException e ) {
                Log.e(LOG_TAG, e.getMessage());
            }

            return false;
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.d(LOG_TAG, "Ignoring SSL certificate error.");
            handler.proceed();
        }
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.e(LOG_TAG, description);
            Log.e(LOG_TAG, failingUrl);
            ((AdobePassDelegate)AdobePassDelegate.getInstance()).dispatchAuthError(AdobePassDelegate.ERROR_TYPE_AUTHN, "Failed loading login page", description);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
        @Override
        public void onLoadResource(WebView view, String url) {
            Log.d(LOG_TAG, "Load Resource: " + url);
            super.onLoadResource(view, url);
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(LOG_TAG, "Page started: " + url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //Page loaded
            Log.d(LOG_TAG, "Page loaded: " + url);
            super.onPageFinished(view, url);
        }

    };


    @Override
    // Only overridden to add some UI for the Graphical Layout
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Only used for the Graphical Layout
        if( isInEditMode() ) {
            Paint backgroundPaint = new Paint();
            backgroundPaint.setColor(Color.WHITE);
            backgroundPaint.setStrokeWidth(20);

            Paint borderPaint = new Paint();
            borderPaint.setColor(Color.BLACK);
            borderPaint.setStrokeWidth(2);
            borderPaint.setStyle(Paint.Style.STROKE);

            Paint titlePaint = new Paint();
            titlePaint.setColor(Color.GRAY);
            titlePaint.setTextAlign(Paint.Align.CENTER);
            titlePaint.setTextSize(26);
            titlePaint.setTypeface(Typeface.DEFAULT_BOLD);

            int height = getMeasuredHeight();
            int width = getMeasuredWidth();
            canvas.drawRoundRect(new RectF(20, 20, width-20, height-20), 20, 20, backgroundPaint);
            canvas.drawRoundRect(new RectF(20, 20, width-20, height-20), 20, 20, borderPaint);

            canvas.drawText("AdobePassLogin WebView", (width)/2, 60, titlePaint);
        }
    }

    public static abstract class CloseListener {
        abstract public void onClose();
    }

}
