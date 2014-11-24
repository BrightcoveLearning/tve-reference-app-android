package com.brightcove.examples.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.brightcove.auth.IAuthDelegate;
import com.brightcove.auth.ap.model.AdobePassConfig;
import com.brightcove.auth.ap.delegates.AdobePassDelegate;
import com.brightcove.auth.model.IProvider;
import com.brightcove.examples.R;
import com.brightcove.examples.adapters.VideoListAdapter;
import com.brightcove.examples.delegates.VideoDelegate;
import com.brightcove.examples.model.VideoItem;
import com.brightcove.examples.model.VideoPlaylistFactory;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BrightcoveVideoView;
import com.brightcove.utils.DownloadImageTask;

import java.util.ArrayList;

/**
 * This is the main application activity
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @since 1.0
 */
public class StartupActivity extends AbstractOptionActivity {

    // Activity Request Code
    private final static int MVPD_PICKER = 33;
    private final static int MVPD_LOGIN = 34;

    // Auth Delegate to manage all the authentication/authorization interaction
    private IAuthDelegate adobePass;
    // The Video Selector GridView
    private GridView videoGrid;
    // Adapter for for the Video Selector GridView
    private VideoListAdapter videoListAdapter;
    // Delegate instance for Validation and video retrieval
    private VideoDelegate videoDelegate;
    // Progress dialog instance to display progress messages
    private ProgressDialog progressDialog;
    // Current context
    private Context currentContext = this;
    // Main View instance, which is used to updated the background with the selected video image
    private LinearLayout linearLayout;

    /**
     * Initiates the main activity, by setting up
     * the {@link com.brightcove.player.view.BrightcoveVideoView} with its {@link com.brightcove.player.event.EventEmitter},
     * the {@link com.brightcove.auth.ap.delegates.AdobePassDelegate} with its various listeners,
     * and then finally the Video Selector GridView with its Adapter
     * @param savedInstanceState ignored by this subclass, and is only passed along to the super class
     * @see com.brightcove.player.view.BrightcoveVideoView
     * @see com.brightcove.player.event.EventEmitter
     * @see com.brightcove.auth.ap.delegates.AdobePassDelegate
     * @see com.brightcove.auth.ap.delegates.AdobePassDelegate#getInstance(android.content.Context, com.brightcove.auth.IAuthConfig, com.brightcove.player.event.EventEmitter)
     * @see com.brightcove.auth.ap.delegates.AdobePassDelegate#init()
     * @see com.brightcove.examples.model.VideoPlaylistFactory#getPlaylist(android.content.Context)
     * @see com.brightcove.examples.adapters.VideoListAdapter
     * @see com.brightcove.examples.delegates.VideoDelegate
     * @since 1.0
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.main);

        // Setting up the BrightcoveVideoView
        // NOTE: This HAS to be done BEFORE calling super.onCreate
        brightcoveVideoView = (BrightcoveVideoView) findViewById(R.id.bcVideoView);
        super.onCreate(savedInstanceState);

        linearLayout = (LinearLayout)brightcoveVideoView.getParent();


        // REGISTER EVENT LISTENERS FOR THE EVENTEMITTER
        EventEmitter eventEmitter = brightcoveVideoView.getEventEmitter();
        // Adding onPlay listener
        eventEmitter.on(EventType.DID_PLAY, onPlayListener );
        eventEmitter.on(EventType.SOURCE_NOT_FOUND, onErrorListener );
        eventEmitter.on(EventType.SOURCE_NOT_PLAYABLE, onErrorListener );
        eventEmitter.on(EventType.ERROR, onErrorListener );
        // Adding Adobe Pass Delegate listener
        eventEmitter.once(AdobePassDelegate.AUTH_INITIATED, initListener);
        eventEmitter.on(AdobePassDelegate.AUTHENTICATED, authenticatedListener);
        eventEmitter.on(AdobePassDelegate.NOT_AUTHENTICATED, notAuthenticatedListener);
        eventEmitter.on(AdobePassDelegate.DISPLAY_PROVIDER_SELECTOR, displayProviderSelectorListener);
        eventEmitter.on(AdobePassDelegate.OPEN_LOGIN_URL, openLoginUrlListener);
        eventEmitter.on(AdobePassDelegate.AUTHORIZED, authorizedListener);
        eventEmitter.on(AdobePassDelegate.NOT_AUTHORIZED, notAuthorizedListener);
        eventEmitter.on(AdobePassDelegate.AUTH_ERROR, authErrorListener);
        // Adding Video Delegate listeners
        eventEmitter.on(VideoDelegate.GOT_VIDEO, gotVideoListener);
        eventEmitter.on(VideoDelegate.VIDEO_ERROR, videoErrorListener);

        // Initiating AdobePass
        adobePass = AdobePassDelegate.getInstance(
                getApplicationContext(),
                new AdobePassConfig(
                        getResources().getString(R.string.requestorId),
                        getResources().getString(R.string.signedRequestorId),
                        getResources().getString(R.string.adobePassEndpoint)
                ),
                eventEmitter
        );
        adobePass.init();


        // Getting the Video metadata and setting up the VideoGrid
        ArrayList<VideoItem> videos = VideoPlaylistFactory.getPlaylist(this);
        videoGrid = (GridView) findViewById(R.id.video_grid_view);
        videoListAdapter = new VideoListAdapter(this, R.layout.video_grid_item, videos);
        videoGrid.setAdapter(videoListAdapter);
        videoGrid.setOnItemClickListener(onVideoItemClickListener);
        videoListAdapter.notifyDataSetChanged();

        // Initiating the VideoDelegate, which handles the Validation and getting the Video rendition(s)
        videoDelegate = new VideoDelegate(eventEmitter);
    }

    /*
     * AdobePass initiation
     * Step 1: AdobePass initiated
     * - Displays Toast message
     * - Enables Login or Logout button
     */
    private EventListener initListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            Boolean isAuthenticated = (Boolean)event.properties.get("isAuthenticated");
            Toast.makeText(getApplicationContext(), "Initiated", Toast.LENGTH_LONG).show();
            videoListAdapter.setAuthenticated(isAuthenticated);
            if( isAuthenticated ) {
                enableLogout();
            }
            else {
                enableLogin();
            }
        }
    };


    /**
     * Authentication Flow.
     * <p>
     * Step 1 - {@code Sign In} Option Menu Item clicked.
     * Starts the AdobePass Authentication process
     * @since 1.0
     */
    @Override
    protected void authenticate() {
        adobePass.authenticate();
    }

    /**
     * Authentication Flow
     * <p>
     * Starts the Logout flow - {@code Sign Out} Option Menu Item clicked
     * @since 1.0
     */
    @Override
    protected void logout() {
        videoListAdapter.setAuthenticated(false);
        brightcoveVideoView.stopPlayback();
        brightcoveVideoView.clear();
        adobePass.logout();
    }

    /*
     * Authentication Flow.
     * Step 2 - AdobePass AuthN listeners
     * - Handles the onAuthenticated
     * - Handles the onNotAuthenticated
     * - Initiates and Starts the Provider Selector Activity
     * - Initiates ans Stars the MVPD Login Activity
     */
    private EventListener authenticatedListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            IProvider provider = (IProvider)event.properties.get("provider");
            enableLogout();
            videoListAdapter.setAuthenticated(true);
            Toast.makeText(getApplicationContext(), "Authenticated: " + provider.getName(), Toast.LENGTH_LONG).show();
        }
    };
    private EventListener notAuthenticatedListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            enableLogin();
            videoListAdapter.setAuthenticated(false);
            Toast.makeText(getApplicationContext(), "Not Authenticated", Toast.LENGTH_LONG).show();
        }
    };
    private EventListener displayProviderSelectorListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            ArrayList<IProvider> providers = (ArrayList<IProvider>)event.properties.get("providers");
            Intent intent = new Intent(getApplicationContext(), MvpdSelectorActivity.class);
            intent.putExtra("providers", providers);
            startActivityForResult(intent, MVPD_PICKER);
        }
    };
    private EventListener openLoginUrlListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            IProvider provider = (IProvider)event.properties.get("provider");
            String url = (String)event.properties.get("url");
            Intent intent = new Intent(getApplicationContext(), MvpdLoginActivity.class);
            intent.putExtra("provider", provider);
            intent.putExtra("url", url);
            startActivityForResult(intent, MVPD_LOGIN);
        }
    };

    /**
     * Authentication Flow
     * <p>
     * Step 3 - Provider selected.
     * Initiates the AdobePass Provider (MVPD) Authentication process
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch( requestCode ) {
            case MVPD_PICKER:
                if( resultCode == RESULT_OK && data != null ) {
                    adobePass.authenticate( (IProvider) data.getParcelableExtra("provider") );
                }
                else {
                    if( progressDialog != null ) progressDialog.dismiss();
                    videoGrid.setEnabled(true);
                    enableInput(true);
                    adobePass.cancelAuthentication();
                }
                break;

            case MVPD_LOGIN:
                if( resultCode == RESULT_CANCELED ) {
                    if( progressDialog != null ) progressDialog.dismiss();
                    videoGrid.setEnabled(true);
                    enableInput(true);
                    adobePass.cancelAuthentication();
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /*
     * Video Selected
     * Step 1: VideoGrid ItemClick handler
     * - Clears the video player
     * - Displays ProgressDialog
     * - Displays the current VideoItem as selected
     * - Disables the VideoGrid and the AdobePass inputs
     * - Get the selected VideoItem
     * - Calls the AdobePass Authorization
     */
    AdapterView.OnItemClickListener onVideoItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            VideoItem videoItem = (VideoItem) adapterView.getItemAtPosition(position);

            brightcoveVideoView.stopPlayback();
            brightcoveVideoView.clear();
            brightcoveVideoView.setVisibility(View.INVISIBLE);


            view.setSelected(true);
            videoGrid.setEnabled(false);
            enableInput(false);

            if( progressDialog != null ) progressDialog.dismiss();
            if( videoItem.getIsProtected() ) {
                progressDialog = ProgressDialog.show(currentContext, "Authorization in Progress", "Please wait...");
                adobePass.authorize(videoItem);
            }
            else {
                progressDialog = ProgressDialog.show(currentContext, "Requesting video", "Please wait...");
                videoDelegate.getVideo(videoItem, null);
            }


            linearLayout.setBackgroundDrawable(null);
            new DownloadImageTask(linearLayout).execute(videoItem.getPoster(), videoItem.getThumbnail());
        }
    };

    /*
     * Video Selected
     * Step 2: Authorization complete, Request Video
     * - Updates ProgressDialog
     * - Requests the Video rendition(s) - Which includes token validation
     */
    private EventListener authorizedListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            if( progressDialog != null ) progressDialog.dismiss();
            progressDialog = ProgressDialog.show(currentContext, "Requesting video", "Please wait...");
            VideoItem videoItem = (VideoItem)event.properties.get("videoItem");
            String shortMediaToken = (String)event.properties.get("shortMediaToken");
            videoDelegate.getVideo(videoItem, shortMediaToken);
        }
    };
    private EventListener notAuthorizedListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            videoGrid.setEnabled(true);
            enableInput(true);
            if( progressDialog != null ) progressDialog.dismiss();
            VideoItem videoItem = (VideoItem)event.properties.get("videoItem");
            String errorCode = (String)event.properties.get("errorCode");
            String errorDetails = (String)event.properties.get("errorDetails");
            showError("Not Authorized", errorCode + ": " + errorDetails);
        }
    };

    /*
     * Video Selected
     * Step 3: Got Video, initiate playback
     * - Adds Video to the player
     * - Calls video start
     * - Updates the ProgressDialog
     */
    private EventListener gotVideoListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            if( progressDialog != null ) progressDialog.dismiss();
            progressDialog = ProgressDialog.show(currentContext, "Loading video", "Please wait...");
            Video video = (Video)event.properties.get("video");
            brightcoveVideoView.add(video);
            brightcoveVideoView.start();
        }
    };
    private EventListener videoErrorListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            videoGrid.setEnabled(true);
            enableInput(true);
            if( progressDialog != null ) progressDialog.dismiss();
            showError("Validation/Video Request", "Cannot retrieve video");
        }
    };


    /*
     * Video Selected
     * Step 4: Video Playing
     * - Enables the VideoGrid
     * - Enables the AdobePass input
     * - Removes the ProgressDialog
     */
    EventListener onPlayListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            brightcoveVideoView.setVisibility(View.VISIBLE);
            videoGrid.setEnabled(true);
            enableInput(true);
            if( progressDialog != null ) progressDialog.dismiss();
        }
    };

    EventListener onErrorListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            videoGrid.setEnabled(true);
            enableInput(true);
            if( progressDialog != null ) progressDialog.dismiss();
            showError("Video Playback", "Cannot playback selected video");
        }
    };

    EventListener authErrorListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            Integer errorType = (Integer)event.properties.get("errorType");
            String errorMessage = (String)event.properties.get("errorMessage");
            String errorDetails = (String)event.properties.get("errorDetails");
            showError(errorMessage, errorDetails);

            switch(errorType) {
                case AdobePassDelegate.ERROR_TYPE_INIT:
                    enableLogout();
                    videoGrid.setEnabled(false);
                    break;
            }
        }
    };

    private void showError(String title, String message) {
        new AlertDialog.Builder(currentContext)
                .setTitle("ERROR: " + title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
