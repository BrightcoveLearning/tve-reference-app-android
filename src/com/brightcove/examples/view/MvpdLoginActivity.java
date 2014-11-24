package com.brightcove.examples.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.brightcove.auth.ap.view.AdobePassLoginView;
import com.brightcove.auth.model.IProvider;
import com.brightcove.examples.R;

/**
 * Login Activity to handle the loading of the {@link com.brightcove.auth.ap.view.AdobePassLoginView}
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @see com.brightcove.auth.ap.view.AdobePassLoginView
 * @since 1.0
 */
public class MvpdLoginActivity extends Activity {
    // Keep the current intent, which will then be used when closing the Login activity
    private Intent currentIntent;

    /**
     * Initiates the Login Activity.
     * Gets the selected provider and url from the Current Intent, and then instantiates the {@link com.brightcove.auth.ap.view.AdobePassLoginView}
     * and loads the Url
     * @param savedInstanceState ignored by this subclass, and is only passed along to the super class
     * @see #getIntent()
     * @see android.content.Intent#getParcelableExtra(String)
     * @see android.content.Intent#getStringExtra(String)
     * @see com.brightcove.auth.ap.view.AdobePassLoginView
     * @since 1.0
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mvpd_login);
        // Get Current Intent
        currentIntent = getIntent();
        // Get the Current Provider from the Intent
        IProvider provider = currentIntent.getParcelableExtra("provider");
        // Get the URL from the Intent
        String url = currentIntent.getStringExtra("url");

        AdobePassLoginView loginView = (AdobePassLoginView) findViewById(R.id.mvpd_login_view);
        loginView.setCloseListener(closeListener);
        loginView.loadUrl(url);
    }

    /**
     * Close listener for the LoginView. Will be invoked when the Mvpd authentication flow is complete
     * @since 1.0
     */
    AdobePassLoginView.CloseListener closeListener = new AdobePassLoginView.CloseListener() {
        @Override
        public void onClose() {
            setResult(RESULT_OK, currentIntent);
            finish();
        }
    };


}