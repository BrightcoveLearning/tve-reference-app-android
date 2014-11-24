package com.brightcove.auth.ap.model;

import com.adobe.adobepass.accessenabler.models.Mvpd;
import com.brightcove.auth.model.IProvider;
import com.brightcove.auth.model.Provider;

import java.util.ArrayList;

/**
 * Factory class to convert the incoming {@link com.adobe.adobepass.accessenabler.models.Mvpd} object(s)
 * into {@link com.brightcove.auth.model.Provider} object(s).
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @see com.adobe.adobepass.accessenabler.models.Mvpd
 * @see com.brightcove.auth.model.IProvider
 * @see com.brightcove.auth.model.Provider
 * @since 1.0
 */
public class ProviderFactory {

    /**
     * Converts an incoming ArrayList of {@link com.adobe.adobepass.accessenabler.models.Mvpd}
     * into an ArrayList of {@link com.brightcove.auth.model.Provider}
     *
     * @param mvpds the array of MVPDs to convert into an array of Providers
     * @return the ArrayList of Provider
     * @since 1.0
     */
    public static ArrayList<IProvider> createProviderArray(ArrayList<Mvpd> mvpds) {
        ArrayList<IProvider> providers = new ArrayList<IProvider>();
        if( mvpds != null ) {
            for( Mvpd mvpd : mvpds ) {
                if( mvpd != null ) {
                    providers.add(createProvider(mvpd));
                }
            }
        }
        return providers;
    }

    /**
     * Converts the incoming {@link com.adobe.adobepass.accessenabler.models.Mvpd}
     * into a {@link com.brightcove.auth.model.Provider}
     *
     * @param mvpd the MVPD to convert into a Provider
     * @return the generated Provider object
     * @since 1.0
     */
    public static IProvider createProvider(Mvpd mvpd) {
        IProvider provider = null;
        if( mvpd != null ) {
            provider = new Provider(mvpd.getId(), mvpd.getDisplayName(), mvpd.getLogoUrl());
        }
        return provider;
    }
}
