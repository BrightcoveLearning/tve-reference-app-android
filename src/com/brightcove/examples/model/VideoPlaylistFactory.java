package com.brightcove.examples.model;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for loading the video items to display,
 * and also for the video url lookup map
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @since 1.0
 */
public class VideoPlaylistFactory {

    private static Context appContext;

    /**
     * Loads and parses the video playlist
     * @param context the application context to use for retrieving the playlist Json file
     * @return an array of video items
     * @since 1.0
     */
    public static ArrayList<VideoItem> getPlaylist(Context context) {
        appContext = context;
        String jsonPlaylist = loadJSONFromAsset("sample_playlist.json");
        VideoPlaylist playlistWrapper = new Gson().fromJson(jsonPlaylist, VideoPlaylist.class);
        return playlistWrapper.playlist;
    }

    /**
     * Loads and parses the video lookup map for the video urls
     * @return the video lookup map for the video urls
     * @since 1.0
     */
    public static Map<String,String> getVideos() {
        String jsonPlaylist = loadJSONFromAsset("sample_videos.json");
        VideoLookup videosWrapper = new Gson().fromJson(jsonPlaylist, VideoLookup.class);
        return videosWrapper.getLookup();
    }

    /**
     * Loads the Json file from the application assets and returns the Json content
     * @param jsonFile the Json file to load from the application assets
     * @return the Json content of the loaded Json file
     * @since 1.0
     */
    private static String loadJSONFromAsset(String jsonFile) {
        String json = null;
        try {
            InputStream is = appContext.getAssets().open(jsonFile);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ioe) {
            Log.e("[VideoPlaylistFactory#loadJSONFromAsset]", ioe.getMessage());
            ioe.printStackTrace();
            return null;
        }
        return json;

    }

    /**
     * Internal class for parsing the Json Playlist
     * @since 1.0
     */
    class VideoPlaylist {
        public ArrayList<VideoItem> playlist;
    }

    /**
     * Internal class for parsing the Json Video Lookup map
     * @since 1.0
     */
    class VideoLookup {
        public ArrayList<VideoItem> videos;

        public Map<String,String> getLookup() {
            Map<String,String> lookup = new HashMap<String,String>();
            for( VideoItem videoItem : videos ) {
                lookup.put(videoItem.getVideoId(), videoItem.getVideo());
            }
            return lookup;
        }
    }

}
