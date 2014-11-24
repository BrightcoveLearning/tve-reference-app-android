package com.brightcove.auth;

import java.util.ArrayList;

/**
 * Interface to encapsulate the configuration data
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @since 1.0
 */
public interface IAuthConfig {
    String getRequestorId();
    String getSignedRequestorId();
    String getEndpoint();
    ArrayList<String> getEndpoints();
}
