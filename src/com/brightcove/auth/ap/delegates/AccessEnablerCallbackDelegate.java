package com.brightcove.auth.ap.delegates;

import com.adobe.adobepass.accessenabler.api.IAccessEnablerDelegate;
import com.adobe.adobepass.accessenabler.models.Event;
import com.adobe.adobepass.accessenabler.models.MetadataKey;
import com.adobe.adobepass.accessenabler.models.MetadataStatus;
import com.adobe.adobepass.accessenabler.models.Mvpd;
import com.brightcove.auth.ap.model.ProviderFactory;
import com.brightcove.auth.model.IProvider;
import com.brightcove.auth.model.IVideoItem;
import com.brightcove.player.event.EventEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages all the callbacks from the underlying Adobe Pass library
 * and emits events back to the UI thread by using the {@link com.brightcove.player.event.EventEmitter}
 * <p>
 * It cannot be instantiate by itself, but must be subclassed first
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @see com.brightcove.auth.ap.delegates.AdobePassDelegate
 * @see com.adobe.adobepass.accessenabler.api.IAccessEnablerDelegate
 * @see com.brightcove.player.event.EventEmitter#on(String, com.brightcove.player.event.EventListener)
 * @since 1.0
 */
public class AccessEnablerCallbackDelegate implements IAccessEnablerDelegate {

    public static final String AUTH_INITIATED = "AuthInitiated";
    public static final String AUTHENTICATED = "Authenticated";
    public static final String NOT_AUTHENTICATED = "NotAuthenticated";
    public static final String GOT_PROVIDER = "GotProvider";
    public static final String NO_PROVIDER = "NoProvider";
    public static final String DISPLAY_PROVIDER_SELECTOR = "DisplayProviderSelector";
    public static final String OPEN_LOGIN_URL = "OpenLoginUrl";
    public static final String AUTHORIZED = "Authorized";
    public static final String NOT_AUTHORIZED = "NotAuthorized";
    public static final String PRE_AUTHORIZED = "PreAuthorized";
    public static final String GOT_METADATA = "GotMetadata";
    public static final String AUTH_TRACKING = "AuthTracking";
    public static final String AUTH_ERROR = "AuthError";

    public static final int ERROR_TYPE_INIT = 0;
    public static final int ERROR_TYPE_AUTHN = 10;
    public static final int ERROR_TYPE_AUTHZ = 20;

    protected static final String INTERNAL_SET_REQUESTOR_COMPLETE = "InternalSetRequestorComplete";
    protected static final String INTERNAL_SET_REQUESTOR_FAILED = "InternalSetRequestorFailed";
    protected static final String INTERNAL_AUTHENTICATED = "InternalAuthenticated";
    protected static final String INTERNAL_NOT_AUTHENTICATED = "InternalNotAuthenticated";
    protected static final String INTERNAL_GOT_PROVIDER = "InternalGotProvider";
    protected static final String INTERNAL_NO_PROVIDER = "InternalNoProvider";
    protected static final String INTERNAL_LOGOUT = "InternalLogout";

    /**
     * Track if the underlying Adobe Pass library has been initiated or not
     */
    protected boolean isInitiated = false;

    /**
     * Currently selected provider
     */
    protected IProvider currentProvider = null;
    /**
     * Indicates if logout is in progress
     */
    protected Boolean isLoggingOut = false;
    /**
     * The currently selected video item
     */
    protected IVideoItem videoItem;

    protected EventEmitter eventEmitter;

    /**
     * Construct a new AccessEnablerCallbackDelegate instance
     * @since 1.0
     */
    protected AccessEnablerCallbackDelegate(EventEmitter eventEmitter) {
        super();
        this.eventEmitter = eventEmitter;
    }

    public void dispatchAuthError(Integer errorType, String errorMessage, String errorDetails) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("errorType", errorType);
        map.put("errorMessage", errorMessage);
        map.put("errorMessage", errorDetails);
        eventEmitter.emit(AUTH_ERROR, map);
    }


    /**
     * Signals that Adobe Pass is initiated and ready to use
     * by emitting the AUTH_INITIATED and AUTHENTICATED or NOT_AUTHENTICATED events
     * @param isAuthenticated true if the user is already authenticated, and false otherwise
     * @param provider the current or last used provider
     * @since 1.0
     */
    protected void initiated(Boolean isAuthenticated, IProvider provider, String errorCode) {
        isInitiated = true;
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("isAuthenticated", isAuthenticated);
        map.put("provider", provider);
        map.put("errorCode", errorCode);
        eventEmitter.emit(AUTH_INITIATED, map);
        String eventType = isAuthenticated ? AUTHENTICATED : NOT_AUTHENTICATED;
        eventEmitter.emit(eventType, map);
    }

    /**
     * Callback method for the Adobe Pass library when the setRequestor call has been fully processed.
     * @param status the 1 if successful, 0 otherwise
     * @see com.brightcove.player.event.EventEmitter#emit(String)
     * @see com.brightcove.player.event.EventEmitter#emit(java.lang.String,java.util.Map)
     * @since 1.0
     */
    @Override
    public void setRequestorComplete(int status) {
        if( status == 0 ) {
            dispatchAuthError(ERROR_TYPE_INIT, "Adobe Pass initialization failed", "The SetRequestor call failed. Please review the requestorId and signedRequestorId");
        }
        else {
            eventEmitter.emit(INTERNAL_SET_REQUESTOR_COMPLETE);
        }
    }

    /**
     * Callback method for Adobe Pass library triggered by either checkAuthentication or getAuthentication
     * Emits the AUTHENTICATED or NOT_AUTHENTICATED events
     * @param status the authentication status (1 for authenticated, or 0 for not authenticated)
     * @param errorCode error message if not authenticated
     * @since 1.0
     */
    @Override
    public void setAuthenticationStatus(int status, String errorCode) {
        isLoggingOut = false;
        Boolean isAuthenticated = (status == 1);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("isAuthenticated", isAuthenticated);
        map.put("provider", currentProvider);
        map.put("errorCode", errorCode);
        String eventType;
        if( isAuthenticated ) {
            eventType = isInitiated ? AUTHENTICATED : INTERNAL_AUTHENTICATED;
        }
        else {
            eventType = isInitiated ? NOT_AUTHENTICATED : INTERNAL_NOT_AUTHENTICATED;
        }
        eventEmitter.emit(eventType, map);
    }

    /**
     * Callback method for Adobe Pass library for successful authorization
     * triggered by either checkAuthorization or getAuthorization.
     * Emits the AUTHORIZED event
     * @param token the Short Media Token which needs to be validated before authorizing video playback
     * @param requestedResourceId the resourceId for which authorization was requested
     * @since 1.0
     */
    @Override
    public void setToken(String token, String requestedResourceId) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("videoItem", videoItem);
        map.put("shortMediaToken", token);
        eventEmitter.emit(AUTHORIZED, map);
        videoItem = null;
    }

    /**
     * Callback method for Adobe Pass library for unsuccessful authorization
     * triggered by either checkAuthorization or getAuthorization.
     * Emits the NOT_AUTHORIZED event
     * @param requestedResourceId the resourceId for which authorization was requested
     * @param errorCode the Adobe Pass provided error code
     * @param errorDescription the MVPD provided error message (if any)
     * @since 1.0
     */
    @Override
    public void tokenRequestFailed(String requestedResourceId, String errorCode, String errorDescription) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("videoItem", videoItem);
        map.put("errorCode", errorCode);
        map.put("errorDetails", errorDescription);
        eventEmitter.emit(NOT_AUTHORIZED, map);
        videoItem = null;
    }

    /**
     * Callback method for Adobe Pass library for the currently selected provider
     * triggered by the getSelectedProvider.
     * Emits the GOT_PROVIDER or NO_PROVIDER events, which includes the converted IProvider from Mvpd
     * @param mvpd the selected Mvpd, which will be converted to a IProvider
     * @see com.brightcove.auth.model.IProvider
     * @since 1.0
     */
    @Override
    public void selectedProvider(Mvpd mvpd) {
        isLoggingOut = false;
        currentProvider = ProviderFactory.createProvider(mvpd);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("provider", currentProvider);
        String eventType;
        if( currentProvider != null ) {
            eventType = isInitiated ? GOT_PROVIDER : INTERNAL_GOT_PROVIDER;
        }
        else {
            eventType = isInitiated ? NO_PROVIDER : INTERNAL_NO_PROVIDER;
        }
        eventEmitter.emit(eventType, map);
    }

    /**
     * Callback method for Adobe Pass library when not authenticated
     * and triggered by getAuthentication
     * Emits the DISPLAY_PROVIDER_SELECTOR event, which includes the ArrayList of providers
     * @param mvpds array of available Mvpds, which will be converted to an array of IProviders
     * @see com.brightcove.auth.model.IProvider
     * @since 1.0
     */
    @Override
    public void displayProviderDialog(ArrayList<Mvpd> mvpds) {
        isLoggingOut = false;
        ArrayList<IProvider> providers = ProviderFactory.createProviderArray(mvpds);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("providers", providers);
        eventEmitter.emit(DISPLAY_PROVIDER_SELECTOR, map);
    }

    /**
     * Callback method for Adobe Pass library with the url to start the Mvpd login process,
     * triggered by the setSelectedProvider
     * Emits the OPEN_LOGIN_URL event, which includes the url and the selected provider
     * @param url the url to to start the Mvpd login process
     * @since 1.0
     */
    @Override
    public void navigateToUrl(String url) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("provider", currentProvider);
        map.put("url", url);
        String eventType = isLoggingOut ? INTERNAL_LOGOUT : OPEN_LOGIN_URL;
        eventEmitter.emit(eventType, map);
    }

    /**
     * Callback method for Adobe Pass library when there is a trackable event
     * Emits the AUTH_TRACKING event
     * @param trackingEvent the tracking event
     * @param data additional tracking data
     * @since 1.0
     */
    @Override
    public void sendTrackingData(Event trackingEvent, ArrayList<String> data) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("eventType", trackingEvent.getType());
        map.put("trackingData", data);
        eventEmitter.emit(AUTH_TRACKING, map);
    }

    /**
     * Callback method for Adobe Pass library when the requested metadata is retrieved
     * Emits the GOT_METADATA event
     * @param key the metadata requested
     * @param result the status and metadata value
     * @see com.adobe.adobepass.accessenabler.models.MetadataKey
     * @see com.adobe.adobepass.accessenabler.models.MetadataStatus
     * @since 1.0
     */
    @Override
    public void setMetadataStatus(MetadataKey key, MetadataStatus result) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("key", key);
        map.put("result", result);
        eventEmitter.emit(GOT_METADATA, map);
    }

    /**
     * Callback method for the Adobe Pass library with the array of resourceIds
     * that could successfully be authorized.
     * Triggered by checkPreauthorizedResources call
     * Emits the PRE_AUTHORIZED event
     * @param resourceIds the resourceIds which could successfully be authorized
     * @since 1.0
     */
    @Override
    public void preauthorizedResources(ArrayList<String> resourceIds) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("resourceIds", resourceIds);
        eventEmitter.emit(PRE_AUTHORIZED, map);
    }
}
