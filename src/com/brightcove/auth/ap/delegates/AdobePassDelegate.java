package com.brightcove.auth.ap.delegates;

import android.util.Log;
import com.adobe.adobepass.accessenabler.api.AccessEnabler;
import com.adobe.adobepass.accessenabler.api.AccessEnablerException;
import com.brightcove.auth.IAuthConfig;
import com.brightcove.auth.IAuthDelegate;
import android.content.Context;
import com.brightcove.auth.ap.view.AdobePassLoginView;
import com.brightcove.auth.model.IProvider;
import com.brightcove.auth.model.IVideoItem;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;

/**
 * Singleton class that manages all the interaction with the underlying Adobe Pass library.
 * Directly handles all the API calls, and using its super class {@link com.brightcove.auth.ap.delegates.AccessEnablerCallbackDelegate}
 * for all the callbacks.
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @see com.brightcove.auth.ap.delegates.AccessEnablerCallbackDelegate
 * @since 1.0
 */
public class AdobePassDelegate extends AccessEnablerCallbackDelegate implements IAuthDelegate {

    // Singleton static instance variable
    private static volatile IAuthDelegate instance;
    // The config used to initiate the Adobe Pass API
    private IAuthConfig config;
    // Application context - Needed to the underlying Adobe Pass API as well as hidden logout WebView
    private Context appContext;
    // Adobe Pass API
    private AccessEnabler accessEnabler;
    private EventEmitterListeners eventEmitterListeners;

    /**
     * Construct a new AccessEnablerCallbackDelegate instance
     * @since 1.0
     */

    /**
     * Private constructor to construct a new AdobePassDelegate instance.
     * Only called the very first time the getInstance is invoked
     * @param appContext the application context, which is used by the Adobe Pass API and hidden logout WebView
     * @param config the config used to initiate the Adobe Pass API
     * @throws AccessEnablerException
     * @see com.brightcove.auth.IAuthConfig
     * @see android.app.Activity#getApplicationContext()
     * @since 1.0
     */
    private AdobePassDelegate(Context appContext, IAuthConfig config, EventEmitter eventEmitter) throws AccessEnablerException {
        super(eventEmitter);
        this.appContext = appContext;
        this.config = config;
        eventEmitterListeners = new EventEmitterListeners(eventEmitter);
        accessEnabler = AccessEnabler.Factory.getInstance(appContext);
        accessEnabler.setDelegate(this);
    }

    /**
     * Static method to get the singleton AdobePassDelegate instance
     * @return the AdobePassDelegate instance
     * @since 1.0
     */
    public static IAuthDelegate getInstance() {
        return instance;
    }

    /**
     * Static method to get or instantiate the singleton AdobePassDelegate instance
     * @param appContext the application context, which is used by the Adobe Pass API and hidden logout WebView
     * @param config the config used to initiate the Adobe Pass API
     * @return the AdobePassDelegate instance
     * @see com.brightcove.auth.IAuthConfig
     * @see android.app.Activity#getApplicationContext()
     * @since 1.0
     */
    public static IAuthDelegate getInstance(Context appContext, IAuthConfig config, EventEmitter eventEmitter) {
        if( instance == null ) {
            synchronized(AdobePassDelegate.class) {
                if( instance == null ) {
                    try {
                        instance = new AdobePassDelegate(appContext, config, eventEmitter);
                    }
                    catch( AccessEnablerException aee ) {
                        Log.e("[AdobePassDelegate]", aee.getMessage());
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Initiates the AdobePassDelegate and the underlying Adobe Pass API
     * and emits AUTH_INITIATED and AUTHENTICATED or NOT_AUTHENTICATED events when completed
     * @see #initiated(Boolean, com.brightcove.auth.model.IProvider, String)
     * @see com.brightcove.player.event.EventEmitter#on(String, com.brightcove.player.event.EventListener)
     * @since 1.0
     */
    public void init() {
        accessEnabler.setRequestor(config.getRequestorId(), config.getSignedRequestorId(), config.getEndpoints());
    }

    /**
     * Initiates the authentication flow
     * and emits AUTHENTICATED or DISPLAY_PROVIDER_SELECTOR events
     * @see #setAuthenticationStatus(int, String)
     * @see #displayProviderDialog(java.util.ArrayList)
     * @see com.brightcove.player.event.EventEmitter#on(String, com.brightcove.player.event.EventListener)
     * @since 1.0
     */
    public void authenticate() {
        if( isInitiated ) {
            accessEnabler.getAuthentication();
        }
        else {
            dispatchAuthError(ERROR_TYPE_AUTHN, "API Not Initiated", "Trying to call authentication before API initiated");
        }
    }

    /**
     * Continues the authentication flow with setting the selected {@link com.brightcove.auth.model.IProvider}
     * and emits OPEN_LOGIN_URL event
     * @see #navigateToUrl(String)
     * @see com.brightcove.player.event.EventEmitter#on(String, com.brightcove.player.event.EventListener)
     * @param provider the selected provider for which to open the login web page
     * @since 1.0
     */
    public void authenticate(IProvider provider) {
        if( isInitiated ) {
            currentProvider = provider;
            accessEnabler.setSelectedProvider(currentProvider.getId());
        }
        else {
            dispatchAuthError(ERROR_TYPE_AUTHN, "API Not Initiated", "Trying to call authentication before API initiated");
        }
    }

    /**
     * Last part of the authentication flow, which will only be called
     * from within {@link android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit.WebView, String)}
     * Emits AUTHENTICATED event
     * @see #setAuthenticationStatus(int, String)
     * @see com.brightcove.player.event.EventEmitter#on(String, com.brightcove.player.event.EventListener)
     * @see com.brightcove.auth.ap.view.AdobePassLoginView
     * @see android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit.WebView, String)
     * @since 1.0
     */
    public void finalizeAuthentication() {
        if( isInitiated ) {
            accessEnabler.getAuthenticationToken();
        }
        else {
            dispatchAuthError(ERROR_TYPE_AUTHN, "API Not Initiated", "Trying to call authentication before API initiated");
        }
    }

    /**
     * Cancels the authentication flow
     * Should be called if the Provider selector or the Login WebView is canceled
     * @since 1.0
     */
    public void cancelAuthentication() {
        if( isInitiated ) {
            currentProvider = null;
            accessEnabler.setSelectedProvider(null);
        }
        else {
            dispatchAuthError(ERROR_TYPE_AUTHN, "API Not Initiated", "Trying to call cancel authentication before API initiated");
        }
    }

    /**
     * Logs out from the current Provider.
     * Emits NOT_AUTHENTICATED event when completed
     * @see #setAuthenticationStatus(int, String)
     * @see com.brightcove.player.event.EventEmitter#on(String, com.brightcove.player.event.EventListener)
     * @since 1.0
     */
    public void logout() {
        if( isInitiated ) {
            isLoggingOut = true;
            accessEnabler.logout();
        }
        else {
            dispatchAuthError(ERROR_TYPE_AUTHN, "API Not Initiated", "Trying to call logout before API initiated");
        }
    }

    /**
     * Starts the authorization flow for the specified resourceId
     * Emits AUTHORIZED or NOT_AUTHORIZED when completed
     * @param resourceId the resourceId to authorize
     * @see #setToken(String, String)
     * @see #tokenRequestFailed(String, String, String)
     * @see com.brightcove.player.event.EventEmitter#on(String, com.brightcove.player.event.EventListener)
     * @since 1.0
     */
    public void authorize(String resourceId) {
        if( isInitiated ) {
            accessEnabler.getAuthorization(resourceId);
        }
        else {
            dispatchAuthError(ERROR_TYPE_AUTHN, "API Not Initiated", "Trying to call authorization before API initiated");
        }
    }

    /**
     * Starts the authorization flow for the specified VideoItem
     * Emits AUTHORIZED or NOT_AUTHORIZED when completed
     * @param videoItem the IVideoItem to authorize
     * @see #setToken(String, String)
     * @see #tokenRequestFailed(String, String, String)
     * @see com.brightcove.player.event.EventEmitter#on(String, com.brightcove.player.event.EventListener)
     * @since 1.0
     */
    public void authorize(IVideoItem videoItem) {
        this.videoItem = videoItem;
        authorize(videoItem.getResourceId());
    }


    private class EventEmitterListeners {
        private EventEmitter eventEmitter;
        private Boolean isAuthenticated;
        private String authNErrorCode;
        private AdobePassLoginView logoutView;

        public EventEmitterListeners(EventEmitter eventEmitter) {
            this.eventEmitter = eventEmitter;
            registerListeners();
        }
        private void registerListeners() {
            eventEmitter.once(INTERNAL_SET_REQUESTOR_COMPLETE, setRequestorListener);
            eventEmitter.once(INTERNAL_AUTHENTICATED, authNListener);
            eventEmitter.once(INTERNAL_NOT_AUTHENTICATED, authNListener);
            eventEmitter.once(INTERNAL_GOT_PROVIDER, providerListener);
            eventEmitter.once(INTERNAL_NO_PROVIDER, providerListener);
            eventEmitter.on(INTERNAL_LOGOUT, logoutListener);
        }

        private EventListener setRequestorListener = new EventListener() {
            @Override
            public void processEvent(Event event) {
                accessEnabler.checkAuthentication();
            }
        };
        private EventListener authNListener = new EventListener() {
            @Override
            public void processEvent(Event event) {
                isAuthenticated = (Boolean)event.properties.get("isAuthenticated");
                authNErrorCode = (String)event.properties.get("errorCode");
                accessEnabler.getSelectedProvider();
            }
        };
        private EventListener providerListener = new EventListener() {
            @Override
            public void processEvent(Event event) {
                IProvider provider = (IProvider) event.properties.get("provider");
                initiated(isAuthenticated, provider, authNErrorCode);
            }
        };
        private EventListener logoutListener = new EventListener() {
            @Override
            public void processEvent(Event event) {
                String url = (String)event.properties.get("url");
                logoutView = new AdobePassLoginView(appContext);
                logoutView.setCloseListener(new AdobePassLoginView.CloseListener() {
                    @Override
                    public void onClose() {
                        accessEnabler.checkAuthentication();
                        logoutView.destroy();
                        logoutView = null;
                    }
                });
                logoutView.loadUrl(url);
            }
        };

    }

}
