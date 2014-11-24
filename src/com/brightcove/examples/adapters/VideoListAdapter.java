package com.brightcove.examples.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.brightcove.examples.R;
import com.brightcove.examples.model.VideoItem;
import com.brightcove.utils.DownloadImageTask;

import java.util.List;

/**
 * ArrayAdapter class for rendering the grid cell items in the VideoGrid view
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @since 1.0
 */
public class VideoListAdapter extends ArrayAdapter<VideoItem> {
    /**
     * ResourceId for the VideoGrid item view container/wrapper
     * @see #getView(int, android.view.View, android.view.ViewGroup)
     * @see com.brightcove.examples.adapters.VideoListAdapter.VideoItemView#setVideoItem(com.brightcove.examples.model.VideoItem)
     * @since 1.0
     */
    private int videoGridItemResource;
    /**
     * Tracking the current authentication state, which is used when rendering the grid item
     * @see #getView(int, android.view.View, android.view.ViewGroup)
     * @see com.brightcove.examples.adapters.VideoListAdapter.VideoItemView#setVideoItem(com.brightcove.examples.model.VideoItem)
     * @since 1.0
     */
    private boolean isAuthenticated = false;
    /**
     * Tracking the currently selected item, which is used when rendering the grid item
     * @see #getView(int, android.view.View, android.view.ViewGroup)
     * @see com.brightcove.examples.adapters.VideoListAdapter.VideoItemView#setVideoItem(com.brightcove.examples.model.VideoItem)
     * @since 1.0
     */
    private VideoItem selectedItem;

    /**
     * Constructs a new VideoListAdapter for rendering the grid items for the VideoGrid
     * @param context the view context for the ArrayAdapter
     * @param videoGridItemResource the for the VideoGrid item view container/wrapper
     * @param videoItems the array of provider items to render
     * @since 1.0
     */
    public VideoListAdapter(Context context, int videoGridItemResource, List<VideoItem> videoItems) {
        super(context, videoGridItemResource, videoItems);
        this.videoGridItemResource = videoGridItemResource;
    }

    /**
     * Sets the current authentication state, and triggers a re-render of the video items
     * @param isAuthenticated the current authentication state
     * @see #getView(int, android.view.View, android.view.ViewGroup)
     * @see com.brightcove.examples.adapters.VideoListAdapter.VideoItemView#setVideoItem(com.brightcove.examples.model.VideoItem)
     * @since 1.0
     */
    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
        notifyDataSetInvalidated();
    }

    /**
     * Updates and returns the grid item view container/wrapper at the provided position for the grid view
     * @param position the item position to update
     * @param convertView the current/previous view container/wrapper for the row item. If null, then a new view will be created
     * @param parent the parent view container
     * @return the grid item view container/wrapper
     * @since 1.0
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VideoItemView videoItemView;

        if (convertView == null) {
            videoItemView = new VideoItemView(getContext(), videoGridItemResource);
        } else {
            videoItemView = (VideoItemView) convertView;
        }

        videoItemView.setVideoItem(getItem(position));
        return videoItemView;
    }

    /**
     * Internal View class for the grid item container/wrapper to allow for simpler update
     * @see #setVideoItem(com.brightcove.examples.model.VideoItem)
     * @since 1.0
     */
    private class VideoItemView extends LinearLayout {
        private ImageView videoThumb;
        private TextView videoTitle;
        private ImageView videoLock;
        private VideoItem videoItem;

        /**
         * Constructs a new container/wrapper view for the grid item
         * @param context the current view context
         * @param resourceId the view resourceId from the /res/layout
         * @since 1.0
         */
        public VideoItemView(Context context, int resourceId) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(resourceId, this, true);

            videoThumb = (ImageView) findViewById(R.id.video_thumbnail);
            videoTitle = (TextView) findViewById(R.id.video_title);
            videoLock = (ImageView) findViewById(R.id.video_lock);
        }

        /**
         * Sets the new VideoItem and renders the VideoGrid item accordingly
         * @param videoItem the VideoItem to render
         * @since 1.0
         */
        public void setVideoItem(VideoItem videoItem) {
            this.videoItem = videoItem;
            updateView();
        }

        /**
         * (Re-)Renders the VideoGrid item
         * @since 1.0
         */
        private void updateView() {
            if( videoItem != null ) {
                // Loads and then displays the video thumbnail
                new DownloadImageTask(videoThumb).execute(videoItem.getThumbnail());
                // Updates the video title text
                videoTitle.setText(videoItem.getTitle());

                // Updates the visibility, background, lock icon
                if( videoItem.getIsProtected() && !isAuthenticated ) {
                    setLocked();
                }
                else if( videoItem.getIsProtected() ) {
                    setUnlocked();
                }
                else {
                    setDefault();
                }

                // Updates the background if the current video item is selected item
                if( videoItem == selectedItem ) {
                    setSelected();
                }
            }
            else {
                videoThumb.setImageBitmap(null);
                videoTitle.setText("");
                setDefault();
            }
        }

        /**
         * Convenience method for updating the visibility, background, lock icon for the LOCKED state
         * @since 1.0
         */
        private void setLocked() {
            setBackgroundDrawable(getResources().getDrawable(R.drawable.video_locked_bg));
            videoLock.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_secure));
            videoLock.setBackgroundColor(getResources().getColor(R.color.color_locked));
            videoLock.setVisibility(VISIBLE);
        }
        /**
         * Convenience method for updating the visibility, background, lock icon for the UNLOCKED state
         * @since 1.0
         */
        private void setUnlocked() {
            setBackgroundDrawable(getResources().getDrawable(R.drawable.video_unlocked_bg));
            videoLock.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_partial_secure));
            videoLock.setBackgroundColor(getResources().getColor(R.color.color_unlocked));
            videoLock.setVisibility(VISIBLE);
        }
        /**
         * Convenience method for updating the visibility, background, lock icon for the DEFAULT state
         * @since 1.0
         */
        private void setDefault() {
            setBackgroundDrawable(getResources().getDrawable(R.drawable.video_default_bg));
            videoLock.setVisibility(INVISIBLE);
        }
        /**
         * Convenience method for updating the background for the SELECTED state
         * @since 1.0
         */
        private void setSelected() {
            setBackgroundDrawable(getResources().getDrawable(R.drawable.video_selected_bg));
        }

        /**
         * Sets the current VideoItem as the selected item, and triggers a re-render of the video items
         * @param isSelected
         * @since 1.0
         */
        @Override
        public void setSelected(boolean isSelected) {
            selectedItem = videoItem;
            notifyDataSetChanged();
        }

    }

}
