package com.brightcove.auth;

import com.brightcove.auth.model.IProvider;
import com.brightcove.auth.model.IVideoItem;

/**
 * Interface for the Auth abstraction delegate
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @since 1.0
 */
public interface IAuthDelegate {
    void init();
    void authenticate();
    void authenticate(IProvider provider);
    void cancelAuthentication();
    void authorize(IVideoItem videoItem);
    void authorize(String resourceId);
    void logout();
}
