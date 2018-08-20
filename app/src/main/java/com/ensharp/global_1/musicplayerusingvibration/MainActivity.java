package com.ensharp.global_1.musicplayerusingvibration;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javazoom.jl.player.Player;

public class MainActivity extends AppCompatActivity implements Serializable, View.OnClickListener {
    private BackPressCloseHandler backPressCloseHandler;
    private EditText searchSong;
    private ListView listView;
    public static ArrayList<MusicVO> list;
    private ArrayList<MusicVO> searchList;
    String TAG = "";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    public static BluetoothConnector btConnector = null; // 블루투스

    private MyAdapter adapter;

    // intent
    private Intent mainIntent;
    private Intent serviceIntent;
    private Intent musicIntent;

    // 현재 재생 중인 노래
    private LinearLayout musicBar;
    private TextView title;
    private TextView singer;
    private ImageView album, previous, play, pause, next;

    // SharedPreferences 파일 변수
    private SharedPreferences preferences;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    // 음악 리스트 생성 후 서비스에 전달
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("preferences", MODE_PRIVATE);

        // 저장소 읽기 권한 얻기 실패하면 종료
        if(!isReadStoragePermissionGranted()) {
            System.exit(0);
        }
        else {
            searchList = new ArrayList<>();
            // 디바이스 안에 있는 mp3 파일 리스트를 조회하여 List 생성
            getMusicList();
            listView = (ListView) findViewById(R.id.listview);

            // 음악리스트에 맞게 어댑터 생성 및 설정
            adapter = new MyAdapter(this, list);
            listView.setAdapter(adapter);

            btConnector = new BluetoothConnector(this,mHandler);
            btConnector.enableBluetooth();

            // intent 설정
            serviceIntent = new Intent(this, PlayerService.class);
            mainIntent = new Intent(this, MainActivity.class);
            musicIntent = new Intent(this, MusicActivity.class);

            // 서비스에 MainActivity, 음악리스트를 전달
            serviceIntent.putExtra("MusicList", (Serializable) list);
            startService(serviceIntent);

            // 뒤로가기 핸들러 설정
            backPressCloseHandler = new BackPressCloseHandler(this);

            // 검색창 생성
            searchSong = (EditText) findViewById(R.id.searchKeyword);
            searchSong.clearFocus();
            searchSong.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchSong.clearComposingText();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // 검색어창에 문자를 입력할때마다 호출.
                    // search 메소드 호출.
                    String input = searchSong.getText().toString();
                    search(input);
                }
            });

            // 하단바 UI 설정
            title = (TextView)findViewById(R.id.currentMusicTitle);
            singer = (TextView)findViewById(R.id.currentMusicSinger);
            album = (ImageView)findViewById(R.id.currentMusicAlbum);
            musicBar = (LinearLayout)findViewById(R.id.currentMusicBar);

            previous = (ImageView)findViewById(R.id.currentMusicPrevious);
            next = (ImageView)findViewById(R.id.currentMusicNext);
            play = (ImageView)findViewById(R.id.currentMusicPlay);
            pause = (ImageView)findViewById(R.id.currentMusicPause);

            pause.setVisibility(View.VISIBLE);
            play.setVisibility(View.GONE);

            previous.setOnClickListener(this);
            next.setOnClickListener(this);
            play.setOnClickListener(this);
            pause.setOnClickListener(this);
            album.setOnClickListener(this);
            musicBar.setOnClickListener(this);

            // 음악리스트에 있는 각 음악들 클릭 이벤트
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // 음악 클릭 시 서비스에 position, list, musicConverter 전달
                    //if(btConnector.checkOnline) {
                        serviceIntent = createServiceIntent();
                        serviceIntent.putExtra("position", position);

                        // preferences 파일 업데이트 후 하단바 업데이트
                        if(!PlayerService.PLAY_STATE) {
                            updateCurrentMusicFile(list, position);
                            updateCurrentMusicPlayerBar();
                        }
                        startService(serviceIntent);
                        startActivity(musicIntent);
                    //}
                    //else {
                    //    btConnector.enableBluetooth();
                    //}
                }
            });
        }
    }

    public Intent createServiceIntent() {
        return new Intent(this, PlayerService.class);
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateCurrentMusicPlayerBar();
        if(PlayerService.PLAY_STATE) {
            pause.setVisibility(View.VISIBLE);
            play.setVisibility(View.GONE);
        }
        else {
            pause.setVisibility(View.GONE);
            play.setVisibility(View.VISIBLE);
        }
    }

    // 앱 나갔을 때 번들에 현재 액티비티 상태 저장
    @Override
    public void onStop(){
        super.onStop();
        getIntent().putExtras(mainIntent);
    }

    // 앱 복귀시 현재 액티비티 상태 복구
    @Override
    public void onResume() {
        super.onResume();
        getIntent().getExtras();
        updateCurrentMusicPlayerBar();
    }

    // 뒤로가기 버튼 클릭 이벤트
    @Override
    public void onBackPressed() {
        Intent serviceIntent = new Intent(this, PlayerService.class);
        // 노래가 재생되고 있는 상태가 아니라면
        if(!PlayerService.PLAY_STATE) {
            if (backPressCloseHandler.onBackPressed()) ;
            stopService(serviceIntent);
        }
        // 액티비티 종료
        else {
            Serializable serializable = null;
            serviceIntent.putExtra("MainActivity", serializable);
            finish();
        }
    }

    // 음악 리스트 얻기
    public void getMusicList(){
        list = new ArrayList<>();

        // 음악의 아이디, 앨범 아이디, 제목, 아티스트 정보 컬럼명 나열하기
        String[] projection = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM };

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

        while(cursor.moveToNext()){
            Uri mp3Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            String filePath = getPathFromUri(getApplication(), mp3Uri);

            // 확장자가 MP3가 아니면 목록에 넣지 않는다.
            if(!filePath.contains(".mp3"))
                continue;

            // 음악 정보 로드하고 musicVO 설정
            MusicVO musicVO = new MusicVO();
            musicVO.setId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            musicVO.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
            musicVO.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            musicVO.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            musicVO.setFilePath(getPathFromUri(getApplication(), mp3Uri));

            // musicVO 리스트에 추가
            list.add(musicVO);
            searchList.add(musicVO);
        }
        cursor.close();
    }

    public void search(String keyWord) {
        //문자 입력시마다 리스트를 지우고 새로 뿌려준다.
        list.clear();

        //문자 입력이 없을 때는 모든 데이터를 보여준다.
        if(keyWord.length() == 0) {
            list.addAll(searchList);
        }

        // 문자 입력할때
        else {
            //리스트의 모든 데이터를 검색한다.
            for(int i = 0; i < searchList.size(); i++) {
                if(searchList.get(i).getTitle().toLowerCase().contains(keyWord)) {
                    list.add(searchList.get(i));
                }
            }
        }
        // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
        adapter.notifyDataSetChanged();
    }

    // 하단바 업데이트
    private void updateCurrentMusicPlayerBar(){
        // 현재 재생 중인 음악이 있으면
        if(preferences != null && preferences.getString("currentMusicAlbum","") != ""){
            // 노래 제목, 가수명 값 불러오기
            title.setText(preferences.getString("currentMusicTitle",""));
            singer.setText(preferences.getString("currentMusicSinger",""));

            Log.e("title",title.getText()+"");
            Log.e("signer",singer.getText()+"");

            // 앨범 이미지 값 불러오기
            byte[] albumBytes = new MyAdapter().getAlbumImage(getApplication(),
                    Integer.parseInt(preferences.getString("currentMusicAlbum","")),
                    170);
            if(albumBytes == null)
                return;
            Bitmap bitmap = BitmapFactory.decodeByteArray(albumBytes, 0, albumBytes.length);
            album.setImageBitmap(bitmap);

            Log.e("preferences","not null, update complete");
        }
        else
            Log.e("preferences","null");
    }

    // preferences 파일 업데이트
    public void updateCurrentMusicFile(List<MusicVO> list, int currentMusicPosition){
        // 현재 음악파일 VO
        MusicVO currentMusicVO = list.get(currentMusicPosition);
        SharedPreferences.Editor editor = preferences.edit();

        // preferences 값 모두 삭제하기
        editor.clear();
        editor.commit();

        // preferences에 현재 재생 음악 정보 저장하기
        editor.putString("currentMusicTitle", currentMusicVO.getTitle());
        editor.putString("currentMusicSinger", currentMusicVO.getArtist());
        editor.putString("currentMusicAlbum", currentMusicVO.getAlbumId());
        editor.commit();
    }

    // 저장소 읽기 권한 확인 여부
    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    // 권한 요청 이벤트
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                Log.d(TAG, "External storage2");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission
                }else{
                    System.exit(0);
                }
                break;

            case 3:
                Log.d(TAG, "External storage1");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission
                }else{
                    System.exit(0);
                }
                break;
        }
    }

    // Uri 경로 얻기
    public static String getPathFromUri(Context context, Uri uri) {
        // 킷캣 여부 확인
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    //블루투스 관련
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        switch(requestCode){
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    btConnector.getDeviceInfo(data);
                }
                else{
                    //취소를 눌렀을때
                    btConnector.checkdouble = false;
                }
                break;
            case REQUEST_ENABLE_BT:
                if(resultCode == Activity.RESULT_OK){
                    //확인 눌렀을때
                    btConnector.scanDevice();
                }
                else{
                    //취소를 눌렀을때
                    btConnector.checkdouble = false;
                }
                break;
        }
    }

//    public void ConnectedThreadStop(){
//        if(mService != null)
//        mService.stop();
//    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    // 하단바 클릭이벤트
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.currentMusicPlay:
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
                serviceIntent.putExtra("PlayerButton", PlayerService.PLAY_BUTTON);
                startService(serviceIntent);
                break;
            case R.id.currentMusicPause:
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                serviceIntent.putExtra("PlayerButton",PlayerService.PAUSE_BUTTON);
                startService(serviceIntent);
                break;
            case R.id.currentMusicPrevious:
                serviceIntent.putExtra("PlayerButton",PlayerService.PREVIOUS_BUTTON);
                startService(serviceIntent);
                if(PlayerService.currentMusicPosition - 1 < 0)
                    updateCurrentMusicFile(list, list.size() - 1);
                else
                    updateCurrentMusicFile(list, PlayerService.currentMusicPosition - 1);
                updateCurrentMusicPlayerBar();
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
                break;
            case R.id.currentMusicNext:
                serviceIntent.putExtra("PlayerButton",PlayerService.NEXT_BUTTON);
                startService(serviceIntent);
                if(PlayerService.currentMusicPosition + 1 >= list.size())
                    updateCurrentMusicFile(list, 0);
                else
                    updateCurrentMusicFile(list, PlayerService.currentMusicPosition + 1);
                updateCurrentMusicPlayerBar();
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
                break;
            case R.id.currentMusicAlbum:
            case R.id.currentMusicBar:
                startActivity(musicIntent);
                break;
        }
    }
}