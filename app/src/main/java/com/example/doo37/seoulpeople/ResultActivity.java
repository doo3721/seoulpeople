package com.example.doo37.seoulpeople;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    // 차트 관련 선언
    LineChart linechart;
    int X_RANGE = 50;
    int DATA_RANGE = 30;

    ArrayList<Entry> xVal;
    LineDataSet setXcomp;
    ArrayList<String> xVals;
    ArrayList<ILineDataSet> lineDataSets;
    LineData lineData;

    // 누적 결과 차트 관련 선언
    BarChart barchart;
    int BAR_X_RANGE = 50;
    int BAR_DATA_RANGE = 30;

    ArrayList<BarEntry> BAR_xVal;
    BarDataSet BAR_setXcomp;
    ArrayList<String> BAR_xVals;
    ArrayList<IBarDataSet> barDataSets;
    BarData barData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ImageView Endbutton = (ImageView) findViewById(R.id.Endbutton);

        // 어플리케이션 종료 버튼 리스너
        Endbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        init(); // 차트 생성 및 옵션 부여

        threadStart(); // 차트 업데이트 스레드
    }


    private void init() { // 차트 생성 함수

        linechart = (LineChart) findViewById(R.id.resultchart);
        barchart = (BarChart) findViewById(R.id.stackchart);

        chartInit();
    }

    private void chartInit() {
        linechart.setAutoScaleMinMaxEnabled(true);

        // chart UI 옵션
        linechart.setBackgroundColor(Color.WHITE);
        linechart.setDrawGridBackground(false);

        linechart.setDescription("");

        // grid line 비활성화
        linechart.getXAxis().setDrawGridLines(false);
        linechart.getAxisLeft().setDrawGridLines(false);
        linechart.getAxisRight().setDrawGridLines(false);

        linechart.getXAxis().setEnabled(false);

        linechart.getAxisRight().setEnabled(false);
        linechart.getAxisLeft().setEnabled(false);
        linechart.getLegend().setEnabled(false);

        xVal = new ArrayList<Entry>();
        setXcomp = new LineDataSet(xVal, "X");

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
        linechart.setData(lineData);
        linechart.invalidate();



        // Bar 차트 관련 옵션

        barchart.setAutoScaleMinMaxEnabled(true);

        // chart UI 옵션
        barchart.setBackgroundColor(Color.WHITE);
        barchart.setDrawGridBackground(false);

        barchart.setDescription("");

        // grid line 비활성화
        barchart.getXAxis().setDrawGridLines(false);
        barchart.getAxisLeft().setDrawGridLines(false);
        barchart.getAxisRight().setDrawGridLines(false);

        barchart.getXAxis().setEnabled(false);

        barchart.getAxisRight().setEnabled(false);
        barchart.getAxisLeft().setEnabled(false);
        barchart.getLegend().setEnabled(false);

        BAR_xVal = new ArrayList<BarEntry>();
        BAR_setXcomp = new BarDataSet(BAR_xVal, "X");

        // 그래프 선 관련 옵션
        BAR_setXcomp.setColor(Color.BLUE);
        BAR_setXcomp.setDrawValues(false);
        BAR_setXcomp.setAxisDependency(YAxis.AxisDependency.LEFT);

        barDataSets = new ArrayList<IBarDataSet>();
        barDataSets.add(BAR_setXcomp);


        BAR_xVals = new ArrayList<String>();
        for (int i = 0; i < BAR_X_RANGE; i++) {
            BAR_xVals.add("");
        }
        barData = new BarData(BAR_xVals,barDataSets);
        barchart.setData(barData);
        barchart.invalidate();

    }

    // 차트 업데이트 함수
    public void chartUpdate(int x) {

        if (xVal.size() > DATA_RANGE) {
            xVal.remove(0);
            for (int i = 0; i < DATA_RANGE; i++) {
                xVal.get(i).setXIndex(i);
            }
        }

        xVal.add(new Entry(x,xVal.size()));
        setXcomp.notifyDataSetChanged();
        linechart.notifyDataSetChanged();
        linechart.invalidate();


        if (BAR_xVal.size() > BAR_DATA_RANGE) {
            BAR_xVal.remove(0);
            for (int i = 0; i < BAR_DATA_RANGE; i++) {
                BAR_xVal.get(i).setXIndex(i);
            }
        }

        BAR_xVal.add(new BarEntry(x,BAR_xVal.size()));
        BAR_setXcomp.notifyDataSetChanged();
        barchart.notifyDataSetChanged();
        barchart.invalidate();
    }

    // 쓰레드 돌릴 핸들러
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                int a = 0;
                a = (int)(Math.random()*100);
                chartUpdate(a);
            }
        }
    };

    // 쓰레드 상속 구문
    class MyThread extends Thread {
        @Override
        public void run() {
            while(true) {
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 쓰레드 시작 함수
    private void threadStart() {
        ResultActivity.MyThread thread = new ResultActivity.MyThread();
        thread.setDaemon(true);
        thread.start();
    }
}
