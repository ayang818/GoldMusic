package cn.hdu.edu.goldmusic;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static cn.hdu.edu.goldmusic.Common.DOWNLOAD_IMG;
import static cn.hdu.edu.goldmusic.Common.FETCH_SONG_DETAIL;
import static cn.hdu.edu.goldmusic.Common.UPDATE_CUR_DURATION;
import static cn.hdu.edu.goldmusic.Common.UPDATE_SEEKBAR;
import static cn.hdu.edu.goldmusic.Common.buildImgDownloadRequests;
import static cn.hdu.edu.goldmusic.Common.buildRequests;

public class PlayerActivity extends AppCompatActivity {
    public static PlayerActivity mView;
    public static MediaPlayer mPlayer;
    public static int curTime;
    ArrayList<Song> musicList;
    public static int curIdx = -1;
    String cookie;
    public static Timer timer;
    public static MyHandler handler = new MyHandler();


    TextView songNameView;
    TextView songPlayer;
    SeekBar seekBar;
    ImageView songImg;
    ImageButton prevBtn;
    ImageButton nextBtn;
    ImageButton playOrPauseBtn;
    TextView startDuration;
    TextView curDuration;
    TextView endDuration;
    TextView quoteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.player_layout);
        init();
    }

    private void init() {
        songNameView = findViewById(R.id.songName);
        songPlayer = findViewById(R.id.songPlayer);
        seekBar = findViewById(R.id.seekBar);
        songImg = findViewById(R.id.songImg);
        prevBtn = findViewById(R.id.prevBtn);
        playOrPauseBtn = findViewById(R.id.playOrPause);
        nextBtn = findViewById(R.id.nextBtn);
        startDuration = findViewById(R.id.startDuration);
        curDuration = findViewById(R.id.curDuration);
        endDuration = findViewById(R.id.endDuration);
        quoteView = findViewById(R.id.quote);

        Intent intent = getIntent();
        musicList = RecommendMusicListActivity.songList;
        curIdx = intent.getIntExtra("targetSong", 0);
        Song curSong = musicList.get(curIdx);

        prepareTextAndImg(curSong);
        if (intent.getBooleanExtra("shouldUpdate", true)) {
            prepareNewSong(curSong);
        } else {
            endDuration.setText(calcMillis2StandardTime(mPlayer.getDuration()));
        }

        playOrPauseBtn.setOnClickListener((v) -> {
            if (mPlayer.isPlaying()) {
                curTime = mPlayer.getCurrentPosition();
                System.out.println(curTime);
                mPlayer.pause();
                playOrPauseBtn.setImageResource(R.drawable.play_status);
            } else {
                mPlayer.seekTo(curTime);
                System.out.println(curTime);
                mPlayer.start();
                playOrPauseBtn.setImageResource(R.drawable.pause_status);
            }
        });

        nextBtn.setOnClickListener((v) -> {
            playNextSong();
        });

        prevBtn.setOnClickListener((v) -> {
            int targetSongIdx;
            if (curIdx - 1 < 0) {
                targetSongIdx = musicList.size() - 1;
            } else {
                targetSongIdx = (curIdx - 1) % musicList.size();
            }
            Song song = musicList.get(targetSongIdx);
            curIdx = targetSongIdx;
            prepareNewSong(song);
            prepareTextAndImg(song);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int duration = (int) (seekBar.getProgress() / 100.0 * mPlayer.getDuration());
                mPlayer.seekTo(duration);
                mPlayer.start();
            }
        });
   }

    private void playNextSong() {
        int targetSongIdx;
        if (curIdx + 1 >= musicList.size()) {
            targetSongIdx = 0;
        } else {
            targetSongIdx = (curIdx + 1) % musicList.size();
        }
        Song song = musicList.get(targetSongIdx);
        curIdx = targetSongIdx;
        prepareNewSong(song);
        prepareTextAndImg(song);
    }

    private void prepareNewSong(Song song) {
        // 更新图片，歌名，时间，SeekBar重置，timer重置
        cookie = RecommendMusicListActivity.vView.cookies;
        String url = "http://119.23.240.115:1080/song/url?id=" + song.getSongId() + "&cookie=" + cookie;
        buildRequests(url, handler, FETCH_SONG_DETAIL);

    }

    private void prepareTextAndImg(Song song) {
        songNameView.setText(song.getSongTitle());
        songPlayer.setText(song.getSongCreator());
        quoteView.setText(Quotes.quoteList[new Random().nextInt(Quotes.quoteList.length)]);
        buildImgDownloadRequests(song.getSongPicUrl(), handler, DOWNLOAD_IMG);
    }

    public void updateSeekBar() {
        int duration = mPlayer.getDuration();
        this.endDuration.setText(calcMillis2StandardTime(duration));
        this.curDuration.setText("00:00");
        reScheduleTimer();
    }

    public String calcMillis2StandardTime(int millisecond) {
        int min = millisecond / 1000 / 60;
        int sec = millisecond / 1000 % 60;
        String minPart = "", secPart = "";
        minPart = min >= 10 ? String.valueOf(min) : "0" + min;
        secPart = sec < 10 ? "0" + sec : String.valueOf(sec);
        return minPart + ":" + secPart;
    }

    private void reScheduleTimer() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // 加判断，避免prepare的时候读取duration
                if (mPlayer.isPlaying()) {
                    // 到ui线程去更新秒数和进度条
                    Message updSeekBarMsg = Message.obtain();
                    updSeekBarMsg.what = UPDATE_SEEKBAR;
                    updSeekBarMsg.obj = (int) (((double) mPlayer.getCurrentPosition() / mPlayer.getDuration()) * 100);
                    Message updCurDurationMsg = Message.obtain();
                    updCurDurationMsg.what = UPDATE_CUR_DURATION;
                    updCurDurationMsg.obj = calcMillis2StandardTime(mPlayer.getCurrentPosition());

                    handler.sendMessage(updSeekBarMsg);
                    handler.sendMessage(updCurDurationMsg);

                    if (Math.abs(mPlayer.getCurrentPosition() - mPlayer.getDuration()) < 1000) {
                        playNextSong();
                    }
                }
            }
        };
        timer.schedule(task, 0, 1000);
    }
}
