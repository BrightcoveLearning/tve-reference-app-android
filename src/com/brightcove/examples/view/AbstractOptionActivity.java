package com.brightcove.examples.view;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.brightcove.examples.R;
import com.brightcove.player.view.BrightcovePlayer;

/**
 * Abstract Activity which extends the BrightcovePlayer activity
 * and sets up the Option Menu
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @since 1.0
 */
public abstract class AbstractOptionActivity extends BrightcovePlayer {

    // Authentication button statuses
    private final int AUTHN_STATUS_LOADING = 0;
    private final int AUTHN_STATUS_LOGIN = 1;
    private final int AUTHN_STATUS_LOGOUT = 2;
    private final int AUTHN_STATUS_ERROR = -1;
    // Current authentication button status
    private int authNStatus = AUTHN_STATUS_LOADING;
    // Authentication Option Menu Item
    private MenuItem menuItemAuthN;

    /**
     * Initiates the option menu
     * @param menu the option menu, in which to add items
     * @return display state - false for hidden, true otherwise
     * @since 1.0
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Gets the authentication option menu item into a local variable
     * @param menu the option menu, in which to add items
     * @return display state - false for hidden, true otherwise
     * @since 1.0
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuItemAuthN = (MenuItem) menu.findItem(R.id.menuItemAuthN);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Process the selected option menu item
     * @param item the selected option menu item
     * @return true to stop further processing, false otherwise
     * @since 1.0
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemClose:
                closeApp();
                break;
            case R.id.menuItemAuthN:
                enableInput(false);
                switch( authNStatus ) {
                    case AUTHN_STATUS_LOGIN:
                        authenticate();
                        break;
                    case AUTHN_STATUS_LOGOUT:
                        logout();
                        break;
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Convenience method setting the LOADING state of the authentication option menu item
     * @since 1.0
     */
    protected void enableLoading() {
        authNStatus = AUTHN_STATUS_LOADING;
        enableInput(false);
        menuItemAuthN.setTitle(R.string.authN_loading);
    }

    /**
     * Convenience method setting the LOGIN state of the authentication option menu item
     * @since 1.0
     */
    protected void enableLogin() {
        authNStatus = AUTHN_STATUS_LOGIN;
        enableInput(true);
        menuItemAuthN.setTitle(R.string.authN_login);
    }

    /**
     * Convenience method setting the LOGOUT state of the authentication option menu item
     * @since 1.0
     */
    protected void enableLogout() {
        authNStatus = AUTHN_STATUS_LOGOUT;
        enableInput(true);
        menuItemAuthN.setTitle(R.string.authN_logout);
    }

    /**
     * Convenience method setting the ERROR state of the authentication option menu item
     * @since 1.0
     */
    protected void enableError() {
        authNStatus = AUTHN_STATUS_ERROR;
        enableInput(false);
        menuItemAuthN.setTitle(R.string.authN_error);
    }

    /**
     * Convenience method for setting the enable state of the  authentication option menu item
     * @since 1.0
     */
    protected void enableInput(Boolean enabled) {
        menuItemAuthN.setEnabled(enabled);
    }

    /**
     * Abstract method for the start of the authentication flow
     * @since 1.0
     */
    protected abstract void authenticate();

    /**
     * Abstract method for the start of the logout flow
     * @since 1.0
     */
    protected abstract void logout();

    /**
     * Close application
     * @since 1.0
     */
    private void closeApp() {
        System.exit(RESULT_OK);
    }

}
