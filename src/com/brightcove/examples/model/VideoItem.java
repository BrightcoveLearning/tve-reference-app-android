package com.brightcove.examples.model;

import com.brightcove.auth.model.IVideoItem;

/**
 * Encapsulates the video metadata.
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @since 1.0
 */
public class VideoItem implements IVideoItem {
    
    private String videoId;
    private String video;
    private String thumbnail;
    private String poster;
    private String title;
    private boolean isProtected;
    private String resourceId;

    /**
     * Gets the video identifier
     * @return the video identifier
     * @since 1.0
     */
    public String getVideoId() {
        return videoId;
    }

    /**
     * Sets the video identifier
     * @param videoId the video identifier
     * @since 1.0
     */
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    /**
     * Gets the video stream url, which should be null for any protected video item
     * @return the video stream url. Null for any protected video item
     * @since 1.0
     */
    public String getVideo() {
        return video;
    }

    /**
     * Sets the video stream url, which should be null for any protected video item
     * @param videoUrl the video stream url. Null for any protected video item
     * @since 1.0
     */
    public void setVideo(String videoUrl) {
        this.video = videoUrl;
    }

    /**
     * Gets the video thumbnail image url
     * @return the video thumbnail image url
     * @since 1.0
     */
    public String getThumbnail() {
        return thumbnail;
    }

    /**
     * Sets the video thumbnail image url
     * @param thumbnailUrl the video thumbnail image url
     * @since 1.0
     */
    public void setThumbnail(String thumbnailUrl) {
        this.thumbnail = thumbnailUrl;
    }

    /**
     * Gets the video poster image url
     * @return the video poster image url
     * @since 1.0
     */
    public String getPoster() {
        return poster;
    }

    /**
     * Sets the video poster image url
     * @param posterUrl the video poster image url
     * @since 1.0
     */
    public void setPoster(String posterUrl) {
        this.poster = posterUrl;
    }

    /**
     * Gets the video title
     * @return the video title
     * @since 1.0
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the video title
     * @param title the video title
     * @since 1.0
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the video protection status
     * @return the video protection status
     * @since 1.0
     */
    public boolean getIsProtected() {
        return isProtected;
    }

    /**
     * Sets the video protection status
     * @param isProtected the video protection status
     * @since 1.0
     */
    public void setIsProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    /**
     * Gets the video resourceId
     * @return the video resourceId
     * @since 1.0
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Sets the video resourceId
     * @param resourceId the video resourceId
     * @since 1.0
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
