package cn.hdu.edu.goldmusic;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Song implements Serializable {
    String songTitle;
    String songCreator;
    String songId;
    String songPicUrl;

    public Song(String songTitle, String songCreator, String songId, String songPicUrl) {
        this.songTitle = songTitle;
        this.songCreator = songCreator;
        this.songId = songId;
        this.songPicUrl = songPicUrl;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongCreator() {
        return songCreator;
    }

    public void setSongCreator(String songCreator) {
        this.songCreator = songCreator;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getSongPicUrl() {
        return songPicUrl;
    }

    public void setSongPicUrl(String songPicUrl) {
        this.songPicUrl = songPicUrl;
    }
}
