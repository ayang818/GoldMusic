package cn.hdu.edu.goldmusic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static cn.hdu.edu.goldmusic.Common.*;

public class RecommendMusicListActivity extends AppCompatActivity {

    public String cookies = "";
    public ListView musicListView;
    public static ArrayList<Song> songList = new ArrayList<>();
    public static RecommendMusicListActivity vView;
    TextView curSongName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_list);
        vView = this;
        if (ContextCompat.checkSelfPermission(RecommendMusicListActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RecommendMusicListActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(RecommendMusicListActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
        if (ContextCompat.checkSelfPermission(RecommendMusicListActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RecommendMusicListActivity.this, Manifest.permission.INTERNET)) {
                ActivityCompat.requestPermissions(RecommendMusicListActivity.this,
                        new String[]{Manifest.permission.INTERNET}, 1);
            }
        }

        MyHandler handler = new MyHandler();
        buildRequests("http://119.23.240.115:1080/login/cellphone?phone=&password=", handler, LOGIN);
        // init the music list
        init();
        buildRequests("http://119.23.240.115:1080/recommend/songs?cookie=" + RecommendMusicListActivity.vView.cookies,  handler, FETCH_RECOMMEND_LIST);
    }

    private void init() {
        musicListView = findViewById(R.id.musicList);
        curSongName = findViewById(R.id.curSongName);
    }
}

class MyHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case LOGIN:
                // 登录
                try {
                    JSONObject json = new JSONObject((String) msg.obj);
                    RecommendMusicListActivity.vView.cookies = json.getString("cookie");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case FETCH_RECOMMEND_LIST:
                // 获取到推荐歌单
                getRecommendList((String) msg.obj);
                break;
            case FETCH_SONG_DETAIL:
                // 获取到歌曲信息并开始播放
                try {
                    JSONObject jsonObject = new JSONObject((String) msg.obj);
                    JSONArray data = jsonObject.getJSONArray("data");
                    JSONObject songObj = (JSONObject) data.get(0);

                    if (PlayerActivity.mPlayer != null) {
                        PlayerActivity.mPlayer.reset();
                    } else {
                        PlayerActivity.mPlayer = new MediaPlayer();
                    }
                    String musicUrl = songObj.getString("url");
                    PlayerActivity.mPlayer.setDataSource(musicUrl);
                    PlayerActivity.mPlayer.prepare();
                    PlayerActivity.mPlayer.start();
                    PlayerActivity.mView.updateSeekBar();
                    RecommendMusicListActivity vView = RecommendMusicListActivity.vView;
                    Song song = RecommendMusicListActivity.songList.get(PlayerActivity.curIdx);
                    vView.curSongName.setText(song.getSongTitle() + "——" + song.getSongCreator());
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                break;
            case DOWNLOAD_IMG:
                Bitmap bitmap = (Bitmap) msg.obj;
                PlayerActivity.mView.songImg.setImageBitmap(bitmap);
                break;
            case UPDATE_SEEKBAR:
                PlayerActivity.mView.seekBar.setProgress((int) msg.obj);
                break;
            case UPDATE_CUR_DURATION:
                PlayerActivity.mView.curDuration.setText((String) msg.obj);
                break;
        }
    }

    public void getRecommendList(String resp) {
        try {
            JSONObject json = new JSONObject(resp);
            String data = json.getString("data");
            System.out.println(data);
            JSONArray dailySongs = new JSONObject(data).getJSONArray("dailySongs");
            for (int i = 0; i < dailySongs.length(); i++) {
                JSONObject tmpJson = (JSONObject) dailySongs.get(i);
                String songName = tmpJson.getString("name");
                String songId = tmpJson.getString("id");
                JSONArray arts = tmpJson.getJSONArray("ar");
                StringBuilder artsStr = new StringBuilder();
                int len = arts.length();
                for (int j = 0; j < len; j++) {
                    if (j != 0) artsStr.append("/");
                    artsStr.append(((JSONObject) arts.get(j)).get("name"));
                }
                String picUrl = tmpJson.getJSONObject("al").getString("picUrl");
                Song song = new Song(songName, artsStr.toString(), songId, picUrl);
                RecommendMusicListActivity.vView.songList.add(song);
                ListAdapter songItemAdapter = new SongItemAdapter(RecommendMusicListActivity.vView, RecommendMusicListActivity.vView.songList);
                RecommendMusicListActivity.vView.musicListView.setAdapter(songItemAdapter);

                RecommendMusicListActivity.vView.musicListView.setOnItemClickListener((parent, view, position, id) -> {
                    Song songInfo = RecommendMusicListActivity.vView.songList.get(position);
                    Log.d("test/songInfo", songInfo.toString());
                    Intent songsSend = new Intent(RecommendMusicListActivity.vView, PlayerActivity.class);
                    songsSend.putExtra("shouldUpdate", PlayerActivity.curIdx != position);
                    songsSend.putExtra("targetSong", position);
                    RecommendMusicListActivity.vView.startActivity(songsSend);
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
