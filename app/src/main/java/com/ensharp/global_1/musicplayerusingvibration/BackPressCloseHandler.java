package com.ensharp.global_1.musicplayerusingvibration;

import android.app.Activity;
import android.widget.Toast;

public class BackPressCloseHandler {
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;

    // 생성자
    public BackPressCloseHandler(Activity activity) {
        // 현재 Activity 받아서 설정
        this.activity = activity;
    }

    // 뒤로가기 버튼 클릭 이벤트
    public void onBackPressed() {
        // 버튼 길게 누를시 안내 메세지 표시
        if(System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        // 버튼 짧게 누르고 현재 노래 정지 상태일 때 프로그램 종료
        if(System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            // if(현재 노래가 정지상태일 때) -> 서비스랑 액티비티 모두 종료
            // else -> 액티비티 종료되고 노래는 계속 재생됨
            android.os.Process.killProcess(android.os.Process.myPid());
            toast.cancel();
        }
    }

    // 안내 메세지
    public void showGuide() {
        toast = Toast.makeText(activity, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}

