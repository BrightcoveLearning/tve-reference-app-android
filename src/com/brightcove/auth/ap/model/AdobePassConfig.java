package com.brightcove.auth.ap.model;

import com.brightcove.auth.IAuthConfig;

import java.util.ArrayList;

/**
 * Holds configuration data for Adobe Pass.
 * <p>
 * <b>NOTE: Never, ever (for production) include the signedRequestorId or the private certificate in the app.
 * ALWAYS load this info at runtime from your own secure server</b>
 * <br>
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @see com.brightcove.auth.IAuthConfig
 * @see com.brightcove.auth.ap.delegates.AdobePassDelegate#getInstance(android.content.Context, com.brightcove.auth.IAuthConfig, com.brightcove.player.event.EventEmitter)
 * @see com.adobe.adobepass.accessenabler.api.AccessEnabler#setRequestor(String, String, java.util.ArrayList)
 * @since 1.0
 */
public class AdobePassConfig implements IAuthConfig {

    private String requestorId;
    private String signedRequestorId;
    private String endpoint;
    private ArrayList<String> endpoints;

    /**
     * Construct a new empty AdobePassConfig instance
     * @since 1.0
     */
    public AdobePassConfig() {
    }
    /**
     * Construct a new AdobePassConfig instance initiated with the provided values
     * @since 1.0
     */
    public AdobePassConfig(String requestorId, String signedRequestorId, String endpoint) {
        setRequestorId(requestorId);
        setSignedRequestorId(signedRequestorId);
        setEndpoint(endpoint);
    }

    /**
     * Gets the Adobe Pass requestorId - Used to identify the app with Adobe Pass
     *
     * @return the requestorId used to identify the app with Adobe Pass
     * @since 1.0
     */
    public String getRequestorId() {
        return requestorId;
    }
    /**
     * Sets the Adobe Pass requestorId - Used to identify the app with Adobe Pass
     *
     * @param requestorId the requestorId used to identify the app with Adobe Pass
     * @since 1.0
     */
    public void setRequestorId(String requestorId) {
        this.requestorId = requestorId;
    }

    /**
     * Gets the signed requestorId which must be signed using your own private certificate.
     * The public part of the certificate must have been supplied to Adobe beforehand.
     * <p>
     * <b>NOTE: Never, ever (for production) include the signedRequestorId or the private certificate in the app.
     * ALWAYS load this info at runtime from your own secure server</b>
     *
     * @return the signed requestorId, signed using your own private certificate and should ALWAYS be loaded at runtime from your own secure server
     * @since 1.0
     */
    public String getSignedRequestorId() {
        return signedRequestorId;
    }
    /**
     * Sets the Signed requestorId which must be signed using your own private certificate.
     * The public part of the certificate must have been supplied to Adobe beforehand.
     * <p>
     * <b>NOTE: Never, ever (for production) include the signedRequestorId or the private certificate in the app.
     * ALWAYS load this info at runtime from your own secure server</b>
     *
     * @param signedRequestorId the signed requestorId, signed using your own private certificate and should ALWAYS be loaded at runtime from your own secure server
     * @since 1.0
     */
    public void setSignedRequestorId(String signedRequestorId) {
        this.signedRequestorId = signedRequestorId;
    }

    /**
     * Gets Adobe Pass service endpoint to be used.
     * This is where one would configure the app to use either Adobe Pass' staging or production environment
     *
     * @return the Adobe Pass service endpoint
     * @since 1.0
     */
    public String getEndpoint() {
        return endpoint;
    }
    /**
     * Sets Adobe Pass service endpoint to be used.
     * This is where one would configure the app to use either Adobe Pass' staging or production environment
     *
     * @param endpoint the Adobe Pass service endpoint
     * @since 1.0
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        ArrayList<String> endpoints = new ArrayList<String>();
        endpoints.add(endpoint);
        setEndpoints(endpoints);
    }

    /**
     * Gets the ArrayList of Adobe Pass service endpoints to be used.
     * This is where one would configure the app to use either Adobe Pass' staging or production environment
     *
     * @return the ArrayList of Adobe Pass service endpoints
     * @since 1.0
     */
    public ArrayList<String> getEndpoints() {
        return endpoints;
    }
    /**
     * Sets the ArrayList of Adobe Pass service endpoints to be used.
     * This is where one would configure the app to use either Adobe Pass' staging or production environment
     *
     * @param endpoints the ArrayList of Adobe Pass service endpoint
     * @since 1.0
     */
    public void setEndpoints(ArrayList<String> endpoints) {
        this.endpoints = endpoints;
    }

}
