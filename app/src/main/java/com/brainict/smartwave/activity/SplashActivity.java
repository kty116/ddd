package com.brainict.smartwave.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import com.brainict.smartwave.R;
import com.brainict.smartwave.common.Commonlib;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;

public class SplashActivity extends AppCompatActivity {

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_splash);

        permissionCheck();

    }

    public void startMainActivity(){
        handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }finally {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 핸드폰 상태 퍼미션 체크
     */
    public void permissionCheck() {

        Commonlib.permissionCheck(this, new String[]{READ_PHONE_STATE},
                true, new Commonlib.PermissionCheckResponseImpl() {
                    @Override
                    public void granted() {
                        startMainActivity();
                    }

                    @Override
                    public void denied() {
                        finish();
                    }
                });
    }

}
