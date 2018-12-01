package com.example.doo37.seoulpeople;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.listener.OnDrawListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResultActivity extends AppCompatActivity implements OnChartValueSelectedListener,
        OnDrawListener {

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

    ArrayList<IBarDataSet> dataSets = null;
    ArrayList<BarEntry> valueSet = new ArrayList<>();
    ArrayList<String> xAxis = new ArrayList<>();


    // 문장 일치율 관련 뷰
    TextView voicediffv;
    TextView sentencediffv;

    String sentenceConsistency = "";

    // 음성 일치율 검사를 위한 변수 목록
    // TODO: 음성 일치율 계산

    // 데이터 누적을 위한 변수 목록

    SQLiteDatabase sqliteDB ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        sqliteDB = init_database();
        init_tables();

        load_values();

        ImageView Endbutton = (ImageView) findViewById(R.id.Endbutton);

        // 어플리케이션 종료 버튼 리스너
        Endbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        init(); // 차트 생성 및 옵션 부여

        voicediffv = (TextView)findViewById(R.id.voicediff);
        sentencediffv = (TextView)findViewById(R.id.sentencedif);


        float stdsLength = ((RecordActivity)RecordActivity.mContext).getstdsLength();
        float usrsLength = ((RecordActivity)RecordActivity.mContext).getusrsLength();

        // 일치율 테스트 함수 반환값을 테스트하기 위한 테스트코드
        /*
        String temp = String.valueOf(stdsLength);
        Toast.makeText(getApplication(), temp, Toast.LENGTH_LONG).show();
        temp = String.valueOf(usrsLength);
        Toast.makeText(getApplication(), temp, Toast.LENGTH_LONG).show();
        */

        float tempConsistency = (usrsLength / stdsLength) * 100;
        String sentenceConsistency = String.valueOf(tempConsistency);

        Toast.makeText(getApplication(), sentenceConsistency +"%", Toast.LENGTH_LONG).show();
        sentencediffv.setText(sentenceConsistency +"%");

        save_values(sentenceConsistency);

        // 누적 데이터 관련 SQLite 관리 부분
    }

    private SQLiteDatabase init_database() {
        // 데이터베이스 처음 생성시 초기화
        SQLiteDatabase db = null;

        // File file = getDatabasePath("contact.db") ;
        File file = new File(getFilesDir(), "contact.db");

        System.out.println("PATH : " + file.toString()) ;

        try {
            db = SQLiteDatabase.openOrCreateDatabase(file, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if (db == null) {
            System.out.println("DB creation failed. " + file.getAbsolutePath());
        } return db;
    }

    private void init_tables() {
        // 테이블 처음 생성시 초기화
        if (sqliteDB != null) {
            String sqlCreateTbl = "CREATE TABLE IF NOT EXISTS CONTACT_T (" +
                    "savedate " + "TEXT NOT NULL," +
                    "voiceC " + "TEXT NOT NULL," +
                    "sentenceC " + "TEXT NOT NULL" + ")";

            System.out.println(sqlCreateTbl);
            sqliteDB.execSQL(sqlCreateTbl);
        }
    }

    private void load_values() {
        if (sqliteDB != null) {
            String sqlQueryTbl = "SELECT * FROM CONTACT_T";
            Cursor cursor = null;

            dataSets = null;
            valueSet = new ArrayList<>();
            int i = 0;
            xAxis = new ArrayList<>();

            // 쿼리 실행
            cursor = sqliteDB.rawQuery(sqlQueryTbl, null);
            cursor.moveToFirst();

            do {
                // 레코드가 존재한다면,

                // 값 가져오기.
                String date = cursor.getString(0);
                // TODO: 액티비티 로드 시 barchart 에 표시될 데이터 로드하기
                String voiceC = cursor.getString(1);
                String sentenceC = cursor.getString(2);

                xAxis.add(date);
                BarEntry newdata = new BarEntry(Float.parseFloat(sentenceC), i); // (value, x index)
                valueSet.add(newdata);
                i++;
            } while (cursor.moveToNext());

            cursor.close();

            BarDataSet barDataSet = new BarDataSet(valueSet, "");
            barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            dataSets = new ArrayList<>();
            dataSets.add(barDataSet);
        }
    }

    private void save_values(String sentenceConsistency) {
        //TODO: 수행 결과를 데이터베이스에 추가하기.

        if (sqliteDB != null) {

            // delete
            //sqliteDB.execSQL("DELETE FROM CONTACT_T") ;

            long now = System.currentTimeMillis();
            Date date = new Date(now);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH-mm-ss-SSS");
            String savedate = sdf.format(date);

            // 임시로 임의 값을 string화 해서 저장
            // TODO: 실제 음성 값을 처리할 것
            String voiceC = "3217";

            String sentenceC = sentenceConsistency;

            String sqlInsert = "INSERT INTO CONTACT_T " +
                    "(savedate, voiceC, sentenceC) VALUES (" +
                    "'" + savedate + "'," +
                    "'" + voiceC + "'," +
                    "'" + sentenceC + "')" ;

            System.out.println(sqlInsert) ;

            sqliteDB.execSQL(sqlInsert) ;
        }
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
        // 현재 화면에 맞게 최대 최소 범위 반응
        //barchart.setAutoScaleMinMaxEnabled(true);

        // chart UI 옵션
        barchart.setBackgroundColor(Color.WHITE);
        barchart.setDrawGridBackground(false);
        barchart.setTouchEnabled(true);
        barchart.setOnChartValueSelectedListener(this);
        barchart.setOnDrawListener(this);

        barchart.setDescription("");

        // grid line 비활성화
        barchart.getXAxis().setDrawGridLines(false);
        barchart.getAxisLeft().setDrawGridLines(false);
        barchart.getAxisRight().setDrawGridLines(false);

        // X 축 항목별 (날짜, 시간 등) 표시
        barchart.getXAxis().setEnabled(true);

        // Y축 오른쪽 값 표시
        barchart.getAxisRight().setEnabled(false);

        // Y축 값 표시
        barchart.getAxisLeft().setEnabled(false);

        // 각 막대별 색상 표시 및 항목 이름 표시
        barchart.getLegend().setEnabled(false);

        BarData bardata = new BarData(xAxis, dataSets);
        bardata.setDrawValues(false);
        barchart.setScaleEnabled(false);
        barchart.setData(bardata);
        barchart.setVisibleXRangeMaximum(4); // allow 20 values to be displayed at once on the x-axis, not more
        barchart.moveViewToX(20); // set the left edge of the chart to x-index 10
                                        // moveViewToX(...) also calls invalidate()
        barchart.animateXY(1000, 1000);
        barchart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("VAL SELECTED",
                "xindex: " + e.getXIndex() + "value:" + e.getVal() + "date : " + e.getData());


        if (sqliteDB != null) {
            String sqlQueryTbl = "SELECT * FROM CONTACT_T" + " WHERE sentenceC =" + e.getVal();

            Cursor cursor = null;

            dataSets = null;
            valueSet = new ArrayList<>();
            int i = 0;
            xAxis = new ArrayList<>();

            // 쿼리 실행
            cursor = sqliteDB.rawQuery(sqlQueryTbl, null);
            if(cursor.moveToFirst()){
                String date = cursor.getString(0);
                String voiceC = cursor.getString(1);
                String sentenceC = cursor.getString(2);
                xAxis.add(date);
                BarEntry newdata = new BarEntry(Float.parseFloat(sentenceC), 1); // (value, x index)
                valueSet.add(newdata);
                sentencediffv.setText(sentenceC +"%");
            }
            cursor.close();

            BarDataSet barDataSet = new BarDataSet(valueSet, "");
            barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            dataSets = new ArrayList<>();
            dataSets.add(barDataSet);
        }

    }

    @Override
    public void onNothingSelected() {
    }

    @Override
    public void onEntryAdded(Entry entry) {

    }

    @Override
    public void onEntryMoved(Entry entry) {

    }

    @Override
    public void onDrawFinished(DataSet<?> dataSet) {

    }
}
