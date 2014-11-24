package com.brightcove.auth.model;

import android.os.Parcelable;

/**
 * Interface to encapsulate the provider data
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @since 1.0
 */
public interface IProvider extends Parcelable {
    String getId();
    void setId(String value);

    String getName();
    void setName(String value);

    String getLogo();
    void setLogo(String value);
}
