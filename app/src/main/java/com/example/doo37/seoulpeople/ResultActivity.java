package com.example.doo37.seoulpeople;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    // 문장 일치율 관련 뷰
    TextView voicediffv;
    TextView sentencediffv;

    String sentenceConsistency = "";

    // 음성 일치율 검사를 위한 변수 목록
    // TODO: 음성 일치율 계산

    // 데이터 누적을 위한 변수 목록
    // TODO: SQLite를 사용한 데이터 저장 및 불러오기

    SQLiteDatabase sqliteDB ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 누적 데이터 관련 SQLite 관리 부분
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
        sentenceConsistency = sentenceConsistency +"%";

        Toast.makeText(getApplication(), sentenceConsistency, Toast.LENGTH_LONG).show();
        sentencediffv.setText(sentenceConsistency);

        // TODO:현재 실행한 테스트를 DB에 저장
        save_values();
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
                    "Date " + "INTEGER NOT NULL," +
                    "voiceC " +
                    "sentenceC" + ")";
            System.out.println(sqlCreateTbl);
            sqliteDB.execSQL(sqlCreateTbl);
        }
    }

    private void load_values() {
        // TODO: 액티비티 로드 시 barchart 에 표시될 데이터 로드하기
        if (sqliteDB != null) {
            String sqlQueryTbl = "SELECT * FROM CONTACT_T";
            Cursor cursor = null;

            // 쿼리 실행
            cursor = sqliteDB.rawQuery(sqlQueryTbl, null);
            if (cursor.moveToNext()) {
                // 레코드가 존재한다면,

                // 값 가져오기.
                int date = cursor.getInt(0);
                int voiceC = cursor.getInt(1);
                int sentenceC = cursor.getInt(2);

            }
        }
    }

    private void save_values() {
        //TODO: 수행 결과를 데이터베이스에 추가하기.

        if (sqliteDB != null) {

            // delete
            sqliteDB.execSQL("DELETE FROM CONTACT_T") ;

            int date = 0;
            int voiceC = 0;
            int sentenceC = 0;

            String sqlInsert = "INSERT INTO CONTACT_T " +
                    "(date, voiceC, sentenceC) VALUES (" +
                    Integer.toString(date) + "," +
                    "'" + Integer.toString(voiceC) + "'," +
                    "'" + Integer.toString(sentenceC) + "'," + ")" ;

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
        //barchart.setAutoScaleMinMaxEnabled(true);

        // chart UI 옵션
        barchart.setBackgroundColor(Color.WHITE);
        barchart.setDrawGridBackground(false);

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

        BarData bardata = new BarData(getXAxisValues(), getDataSet());
        bardata.setDrawValues(false);
        barchart.setScaleEnabled(false);
        barchart.setData(bardata);
        barchart.setVisibleXRangeMaximum(4); // allow 20 values to be displayed at once on the x-axis, not more
        barchart.moveViewToX(20); // set the left edge of the chart to x-index 10
                                        // moveViewToX(...) also calls invalidate()
        barchart.animateXY(1000, 1000);
        barchart.invalidate();

    }

    private ArrayList<IBarDataSet> getDataSet() {
        //TODO: DB에 저장된 데이터 받아서 밸류로 추가하기

        ArrayList<IBarDataSet> dataSets = null;

        ArrayList<BarEntry> valueSet2 = new ArrayList<>();
        BarEntry v2e1 = new BarEntry(150.000f, 0); // (value, x index)
        valueSet2.add(v2e1);
        BarEntry v2e2 = new BarEntry(90.000f, 1);
        valueSet2.add(v2e2);
        BarEntry v2e3 = new BarEntry(120.000f, 2);
        valueSet2.add(v2e3);
        BarEntry v2e4 = new BarEntry(60.000f, 3);
        valueSet2.add(v2e4);
        BarEntry v2e5 = new BarEntry(20.000f, 4);
        valueSet2.add(v2e5);
        BarEntry v2e6 = new BarEntry(80.000f, 5);
        valueSet2.add(v2e6);
        BarEntry v2e7 = new BarEntry(150.000f, 6); // (value, x index)
        valueSet2.add(v2e7);
        BarEntry v2e8 = new BarEntry(90.000f, 7);
        valueSet2.add(v2e8);
        BarEntry v2e9 = new BarEntry(120.000f, 8);
        valueSet2.add(v2e9);
        BarEntry v2e10 = new BarEntry(60.000f, 9);
        valueSet2.add(v2e10);
        BarEntry v2e11 = new BarEntry(20.000f, 10);
        valueSet2.add(v2e11);
        BarEntry v2e12 = new BarEntry(80.000f, 11);
        valueSet2.add(v2e12);

        BarDataSet barDataSet2 = new BarDataSet(valueSet2, "");
        barDataSet2.setColors(ColorTemplate.COLORFUL_COLORS);

        dataSets = new ArrayList<>();
        dataSets.add(barDataSet2);

        return dataSets;
    }

    private ArrayList<String> getXAxisValues() {
        //TODO: DB에 저장된 날짜 받아서 X 축으로 추가하기

        ArrayList<String> xAxis = new ArrayList<>();
        xAxis.add("2018-12-01");
        xAxis.add("2018-12-02");
        xAxis.add("2018-12-03");
        xAxis.add("2018-12-04");
        xAxis.add("2018-12-05");
        xAxis.add("2018-12-06");
        xAxis.add("2018-12-07");
        xAxis.add("2018-12-08");
        xAxis.add("2018-12-09");
        xAxis.add("2018-12-10");
        xAxis.add("2018-12-11");
        xAxis.add("2018-12-12");
        return xAxis;
    }
}
