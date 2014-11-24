package com.brightcove.auth.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Encapsulate the provider data
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @since 1.0
 */
public class Provider implements IProvider {

    private String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    private String logo;
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }


    public Provider() {
        super();
    }
    public Provider(String id, String name, String logo) {
        super();
        setId(id);
        setName(name);
        setLogo(logo);
    }

    private Provider(Parcel in) {
        super();
        setId(in.readString());
        setName(in.readString());
        setLogo(in.readString());
    }

    @Override
    public int describeContents() { return 0; }
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(logo);
    }

    public static final Parcelable.Creator<Provider> CREATOR = new Parcelable.Creator<Provider>() {
        @Override
        public Provider createFromParcel(Parcel in) { return new Provider(in); }
        @Override
        public Provider[] newArray(int size) { return new Provider[size]; }
    };


}
