package com.ensharp.global_1.musicplayerusingvibration;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;

import java.io.File;
import java.util.ArrayList;

public class MusicActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private ArrayList<MusicVO> list;
    private LinearLayout topLayout;
    private TextView title;
    private TextView singer;
    private ImageView album,previous,play,pause,next;
    private TextView lyrics;
    private SeekBar seekBar;
    private ProgressUpdate progressUpdate;
    private int position;
    private boolean isPlaying;
    private PopupMenu menu;

    private boolean mBound = false;
    // Intent 선언
    private Intent serviceIntent;
    private PlayerService mService;
    private MusicActivity musicActivity = this;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            isPlaying = true;

            // 현재 뮤직 액티비티 변경
            mService.changeMusicActivity(musicActivity);

            // 서비스에서 현재 음악파일 position 과 음악 리스트 가져오기
            position = mService.getCurrentMusicPosition();
            list = mService.getMusicList();

            setMusicContents(list.get(position));

            progressUpdate = new ProgressUpdate();
            progressUpdate.start();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        // UI적인 요소들 설정
        title = (TextView)findViewById(R.id.title);
        singer = (TextView)findViewById(R.id.singer);
        album = (ImageView)findViewById(R.id.album);
        seekBar = (SeekBar)findViewById(R.id.seekbar);
        topLayout = (LinearLayout)findViewById(R.id.musicAct);
        previous = (ImageView)findViewById(R.id.pre);
        play = (ImageView)findViewById(R.id.play);
        pause = (ImageView)findViewById(R.id.pause);
        next = (ImageView)findViewById(R.id.next);
        lyrics = (TextView)findViewById(R.id.lyrics);
        lyrics.setMovementMethod(new ScrollingMovementMethod());

        serviceIntent = new Intent(this, PlayerService.class);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

        // 각 재생 관련 버튼들 클릭 이벤트 리스너 설정
        previous.setOnClickListener(this);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        next.setOnClickListener(this);

        play.setVisibility(View.GONE);
        pause.setVisibility(View.VISIBLE);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                serviceIntent.putExtra("PlayerButton",PlayerService.PAUSE_BUTTON);
                startService(serviceIntent);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 음악 프레임 위치 이동
                if (seekBar.getProgress() >= 200) {
                    seekBar.setProgress(199);
                    mService.setFrame(199);
                } else
                    mService.setFrame(seekBar.getProgress());
                serviceIntent.putExtra("PlayerButton", PlayerService.PLAY_BUTTON);
                startService(serviceIntent);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    public static Bitmap blur(Context context, Bitmap sentBitmap, int radius) {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

            final RenderScript rs = RenderScript.create(context);
            final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT);
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(radius); //0.0f ~ 25.0f
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmap);
            return bitmap;
        }

        return null;
    }

    public void setMusicContents(MusicVO musicDto) {
        while (mService.isConverting());
        try {
            seekBar.setProgress(0);
            File file = new File(musicDto.getFilePath());
            try{
                // MP3 파일 로드
                MP3File mp3 = (MP3File) AudioFileIO.read(file);

                AbstractID3v2Tag tag2 = mp3.getID3v2Tag();
                if(tag2 != null)
                    musicDto.setLyrics(tag2.toString());
            }catch(Exception e) {
                e.printStackTrace();
            }

            title.setText(musicDto.getTitle());
            singer.setText(musicDto.getArtist());
            seekBar.setMax(210);

            // byte 배열로 압축된 비트맵 이미지를 다시 변환함
            byte[] albumBytes = new MyAdapter().getAlbumImage(getApplication(), Integer.parseInt(musicDto.getAlbumId()), 170);
            Bitmap bitmap = BitmapFactory.decodeByteArray(albumBytes, 0, albumBytes.length);
            album.setImageBitmap(bitmap);

            // 앨범 이미지를 blur하고 어둡게 해서 배경으로 설정
            Drawable drawable = new BitmapDrawable(blur(this, bitmap, 25));
            drawable.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY);
            topLayout.setBackground(drawable);


            if(musicDto.getLyrics().contains("Lyrics")) {
                int lyricsFirstLocation = musicDto.getLyrics().indexOf("Lyrics=") + 8;
                int lyricsLastLocation = musicDto.getLyrics().indexOf("\"",lyricsFirstLocation);
                lyrics.setText(musicDto.getLyrics().substring(lyricsFirstLocation, lyricsLastLocation));
            }
        }
        catch (Exception e) {
            Log.e("SimplePlayer", e.getMessage());
        }

        serviceIntent.putExtra("PlayerButton",PlayerService.PLAY_BUTTON);
        if(!mService.isPlaying())
            startService(serviceIntent);
    }

    public void showMenu(View v) {
        menu = new PopupMenu(this, v);
        menu.setOnMenuItemClickListener(this);
        menu.inflate(R.menu.menu_music);
        menu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == menu.getMenu().getItem(0).getItemId())
            mService.changeFilter(MusicConverter.TOUGH);
        else if (item.getItemId() == menu.getMenu().getItem(1).getItemId())
            mService.changeFilter(MusicConverter.DELICACY);

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play:
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
                serviceIntent.putExtra("PlayerButton",PlayerService.PLAY_BUTTON);
                startService(serviceIntent);
                break;
            case R.id.pause:
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                serviceIntent.putExtra("PlayerButton",PlayerService.PAUSE_BUTTON);
                startService(serviceIntent);
                break;
            case R.id.pre:
                serviceIntent.putExtra("PlayerButton",PlayerService.PREVIOUS_BUTTON);
                startService(serviceIntent);
                setMusicContents(mService.getCurrentMusicVO(PlayerService.PREVIOUS_BUTTON));
                break;
            case R.id.next:
                serviceIntent.putExtra("PlayerButton",PlayerService.NEXT_BUTTON);
                startService(serviceIntent);
                setMusicContents(mService.getCurrentMusicVO(PlayerService.NEXT_BUTTON));
                break;
        }
    }

    public void syncWithNotification(int button) {
        switch (button) {
            case PlayerService.PAUSE_BUTTON:
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                break;
            case PlayerService.PLAY_BUTTON:
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
                break;
            case PlayerService.PREVIOUS_BUTTON:
                setMusicContents(mService.getCurrentMusicVO(PlayerService.PREVIOUS_BUTTON));
                break;
            case PlayerService.NEXT_BUTTON:
                setMusicContents(mService.getCurrentMusicVO(PlayerService.NEXT_BUTTON));
                break;
        }
    }

    class ProgressUpdate extends Thread{
        @Override
        public void run() {
            while(mBound && mService != null){
                try {
                    // 노래가 다 끝나면 다음 곡 재생
                    if(mService.isCompletePlay()) {
                        setMusicContents(mService.getCurrentMusicVO(PlayerService.NEXT_BUTTON));
                        serviceIntent.putExtra("PlayerButton", PlayerService.NEXT_BUTTON);
                        startService(serviceIntent);
                    }
                    Thread.sleep(500);
                    if(mService!=null)
                        seekBar.setProgress(mService.getCurrentProgress());
                } catch (Exception e) {
                    Log.e("ProgressUpdate",e.getMessage());
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        mService.changeMusicActivity(null);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }
}