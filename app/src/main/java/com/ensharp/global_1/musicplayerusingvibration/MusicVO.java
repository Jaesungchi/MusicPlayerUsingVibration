package com.ensharp.global_1.musicplayerusingvibration;

/**
 * Created by sukholim on 2018. 7. 23..
 */

import java.io.Serializable;

public class MusicVO implements Serializable {
    private String id;
    private String albumId;
    private String title;
    private String artist;
    private String lyrics;
    private String filePath;

    public MusicVO() {
    }

    public MusicVO(String id, String albumId, String title, String artist, String lyrics, String filePath) {
        this.id = id;
        this.albumId = albumId;
        this.title = title;
        this.artist = artist;
        this.lyrics = lyrics;
        this.filePath = filePath;
    }

    public String getId() { return id; }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    public void setLyrics(String lyrics) { this.lyrics = lyrics; }

    public String getLyrics() { return lyrics; }

    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFilePath() { return filePath; }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String toString() {
        return "MusicDto{" +
                "id='" + id + '\'' +
                ", albumId='" + albumId + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }
}

