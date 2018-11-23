package com.example.doo37.seoulpeople;

import android.content.Intent;
import android.graphics.Color;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // 차트 관련 선언
    LineChart chart;
    int X_RANGE = 50;
    int DATA_RANGE = 40;

    ArrayList<Entry> xVal;
    LineDataSet setXcomp;
    ArrayList<String> xVals;
    ArrayList<ILineDataSet> lineDataSets;
    LineData lineData;

    private DetectNoise mSensor;
    private Context ct = this;
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensor = new DetectNoise();

        // 레코딩 퍼미션 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            threadStart();
        }
        else {
            boolean isGranted = true;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_RECORD_AUDIO);
            // 퍼미션 허용이 될 때까지 반복
            while(isGranted) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                }
            }
            threadStart();
        }

        init();

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
                mSensor.stop();
                handler.removeMessages(0);
                Intent intent = new Intent(
                        getApplicationContext(), RecordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void init(){
        chart = (LineChart) findViewById(R.id.chart);
        chartInit();
    }


    private void chartInit() {
        chart.setAutoScaleMinMaxEnabled(true);

        // chart UI 옵션
        chart.setBackgroundColor(Color.WHITE);
        //chart.setDrawGridBackground(false);

        // chart.setDescription("");

        // grid line 비활성화
        //chart.getXAxis().setDrawGridLines(false);
        //chart.getAxisLeft().setDrawGridLines(false);
        //chart.getAxisRight().setDrawGridLines(false);

        chart.getAxisRight().setAxisMinValue(-2.0f);
        chart.getAxisLeft().setAxisMinValue(-2.0f);
        chart.getAxisRight().setAxisMaxValue(16.0f);
        chart.getAxisLeft().setAxisMaxValue(16.0f);
        chart.getXAxis().setEnabled(false);
        //chart.getAxisRight().setEnabled(false);
        //chart.getAxisLeft().setEnabled(false);
        // chart.getLegend().setEnabled(false);

        xVal = new ArrayList<Entry>();
        setXcomp = new LineDataSet(xVal, "내 음성");

        // 그래프 선 관련 옵션
        setXcomp.setColor(Color.BLUE);
        setXcomp.setDrawValues(false);
        setXcomp.setDrawCircles(false);
        setXcomp.setAxisDependency(YAxis.AxisDependency.LEFT);
        setXcomp.setDrawCubic(true);

        lineDataSets = new ArrayList<ILineDataSet>();
        lineDataSets.add(setXcomp);

        xVals = new ArrayList<String>();
        for (int i = 0; i < X_RANGE; i++) {
            xVals.add("");
        }
        lineData = new LineData(xVals,lineDataSets);
        chart.setData(lineData);
        chart.invalidate();
    }

    // 차트 업데이트 함수
    public void chartUpdate(float x) {

        if (xVal.size() > DATA_RANGE) {
            xVal.remove(0);
            for (int i = 0; i < DATA_RANGE; i++) {
                xVal.get(i).setXIndex(i);
            }
        }

        xVal.add(new Entry(x,xVal.size()));
        setXcomp.notifyDataSetChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    // 쓰레드 돌릴 핸들러
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                double amp = mSensor.getAmplitude();
                if (amp < 0)
                    amp = 0;
                else if (amp > 12)
                    amp = Math.sqrt(amp - 12) + 12;
                float amp2 = (float) amp;
                chartUpdate(amp2);
            }
        }
    };


    // 핸들러 상속 구문
    class MyThread extends Thread {
        @Override
        public void run() {

            while(true) {
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 쓰레드 시작 함수
    private void threadStart() {
        MyThread thread = new MyThread();
        thread.setDaemon(true);
        mSensor.start();
        thread.start();
    }
}
