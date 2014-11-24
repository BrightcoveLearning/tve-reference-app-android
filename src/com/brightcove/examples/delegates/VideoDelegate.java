package com.brightcove.examples.delegates;

import android.os.AsyncTask;
import android.util.Log;
import com.brightcove.auth.model.IVideoItem;
import com.brightcove.examples.model.VideoPlaylistFactory;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.model.Video;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the validation and retrieval of the authorized video.
 * It uses an internal AsyncTask to process the validation and retrieving the video,
 * and then uses the {@link com.brightcove.player.event.EventEmitter} to emit the GOT_VIDEO event
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @see com.brightcove.player.event.EventEmitter#on(String, com.brightcove.player.event.EventListener)
 * @since 1.0
 */
public class VideoDelegate {

    public static final String GOT_VIDEO = "GotVideo";
    public static final String VIDEO_ERROR = "VideoError";

    // Used to emits events when video actions are completed
    private EventEmitter eventEmitter;

    /**
     * Constructs a new VideoDelegate instance to use for validate and retrieve the selected video
     * @param eventEmitter the emitter to use for dispatching video events
     * @since 1.0
     */
    public VideoDelegate(EventEmitter eventEmitter) {
        this.eventEmitter = eventEmitter;
    }

    /**
     * Starts the validation and retrieval process
     * @param videoId the video identifier of the authorized video to retrieve
     * @param shortMediaToken AdobePass ShortMediaToken to validate
     * @since 1.0
     */
    public void getVideo(String videoId, String shortMediaToken) {
        new GetVideoTask().execute(videoId,shortMediaToken);
    }

    /**
     * Starts the validation and retrieval process
     * @param videoItem the authorized video item to retrieve
     * @param shortMediaToken AdobePass ShortMediaToken to validate
     * @since 1.0
     */
    public void getVideo(IVideoItem videoItem, String shortMediaToken) {
        getVideo(videoItem.getVideoId(), shortMediaToken);
    }

    /**
     * Internal AsyncTask class to handle the validation and retrieval of the authorized video
     * @since 1.0
     */
    private class GetVideoTask extends AsyncTask<String, Void, VideoResult> {
        /**
         * Fake url to the validation and video retrieval service.
         * This should be replaced with the actual service endpoint.
         * @see #getVideo(String, String)
         * @since 1.0
         */
        private final String FAKE_VALIDATION_AND_VIDEO_SERVICE = "https://goggle.com";

        /**
         * Background process for validating and retrieving the authorized video
         * @param videoParams string array with videoId and shortMediaToken
         * @return the VideoResult object containing the videoUrl and validation status
         * @since 1.0
         */
        @Override
        protected VideoResult doInBackground(String... videoParams) {
            String videoId = videoParams[0];
            String shortMediaToken = videoParams[1];
            return getVideo(videoId, shortMediaToken);
        }

        /**
         * Emits the GOT_VIDEO event by using the {@link com.brightcove.player.event.EventEmitter}
         * @param videoResult the VideoResult object containing the videoUrl and validation status
         * @since 1.0
         */
        @Override
        protected void onPostExecute(VideoResult videoResult) {
            Map<String,Object> map = new HashMap<String, Object>();
            if( videoResult.isSuccessful() ) {
                Video video = Video.createVideo(videoResult.getVideoUrl());
                map.put("video", video);
                eventEmitter.emit(GOT_VIDEO, map);
            }
            else {
                eventEmitter.emit(VIDEO_ERROR, map);
            }
        }

        /**
         * The actual validation and video retrieval method, invoked by {@link #doInBackground(String...)}
         * @param videoId the video identifier of the authorized video to retrieve
         * @param shortMediaToken AdobePass ShortMediaToken to validate
         * @return the VideoResult object containing the videoUrl and validation status
         * @since 1.0
         */
        private VideoResult getVideo(String videoId, String shortMediaToken) {
            if( shortMediaToken != null ) {
                try {
                    //Fake URL request - Should be replaced with a real validation and video request
                    URL url = new URL(FAKE_VALIDATION_AND_VIDEO_SERVICE);
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    urlConnection.connect();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    } finally {
                        urlConnection.disconnect();
                    }
                }
                catch (MalformedURLException murle) {
                    // Currently only loading a FAKE url to mimic the validation and retrieval request
                    Log.d("[VideoDelegate#getVideo]", murle.getMessage());
                }
                catch (IOException ioe) {
                    // Currently only loading a FAKE url to mimic the validation and retrieval request
                    Log.d("[VideoDelegate#getVideo]", ioe.getMessage());
                }
            }

            // Getting the video url from an embedded resource
            Map<String,String> lookup = VideoPlaylistFactory.getVideos();
            return new VideoResult(lookup.get(videoId));
        }
    }

    /**
     * Holds the video result from validating and retrieving the requested video
     * @since 1.0
     */
    private class VideoResult {
        private String videoUrl;

        /**
         * Constructs a new VideoResult with the retrieved video url.
         * The url would be null if the validation failed
         * @param videoUrl the url of the retrieved video url, which would be null if the validation failed
         * @since 1.0
         */
        public VideoResult(String videoUrl) {
            this.videoUrl = videoUrl;
        }

        public String getVideoUrl() { return videoUrl; }
        public Boolean isSuccessful() { return videoUrl != null; }
    }
}