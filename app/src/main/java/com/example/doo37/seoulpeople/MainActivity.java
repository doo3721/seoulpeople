package com.example.doo37.seoulpeople;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    private Context ct = this;
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView helpbutton = (ImageView) findViewById(R.id.question);

        ImageView startrecord = (ImageView) findViewById(R.id.bt_start);

        // 도움말 보기 버튼 리스너
        helpbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                Intent intent = new Intent(
                        getApplicationContext(), IntroActivity.class);
                startActivity(intent);
            }
        });

        // next 액티비티 전환 리스너
        startrecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                Intent intent = new Intent(
                        getApplicationContext(), RecordActivity.class);
                startActivity(intent);
            }
        });

        // 레코딩 퍼미션 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO
                );
            }
        }
    }
}
