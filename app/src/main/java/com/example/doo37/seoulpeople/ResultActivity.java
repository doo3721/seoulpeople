package com.example.doo37.seoulpeople;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
    ArrayList<Entry> xVal2;
    LineDataSet setXcomp2;
    ArrayList<String> xVals2;

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
    ArrayList<Float> sttRmsList = new ArrayList<>();           // 음성 rms수치 리스트
    ArrayList<Float> soundAmpList = new ArrayList<>();          // 음성파일 amp수치 리스트

    // 데이터 누적을 위한 변수 목록
    private int indexi = 0;
    SQLiteDatabase sqliteDB ;
    int numi = 0;

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

        ArrayList<Float> soundAmpList = ((RecordActivity)RecordActivity.mContext).getstdsentence();
        ArrayList<Float> sttRmsList = ((RecordActivity)RecordActivity.mContext).getusrsentence();

        soundAmpListGraph(soundAmpList);
        sttRmsListGraph(sttRmsList);

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

        save_values(soundAmpList, sttRmsList, sentenceConsistency);

        // 누적 데이터 관련 SQLite 관리 부분
    }
    public void LoadAmpGraph(String AmpList) {

        String[] array = AmpList.split(" ");
        for(int i=0; i<array.length; i++) {
            xVal.add(new Entry(Float.parseFloat(array[i]), i+1));
            //Toast.makeText(getApplication(), array[i], Toast.LENGTH_LONG).show();
        }
        setXcomp.notifyDataSetChanged();
        linechart.notifyDataSetChanged();
        linechart.invalidate();
    }

    public void LoadRmsGraph(String RmsList) {

        String[] array = RmsList.split(" ");
        for(int i=0; i<array.length; i++) {
            xVal2.add(new Entry(Float.parseFloat(array[i]), i+1));
            //Toast.makeText(getApplication(), array[i], Toast.LENGTH_LONG).show();
        }
        setXcomp2.notifyDataSetChanged();
        linechart.notifyDataSetChanged();
        linechart.invalidate();
    }

    public void soundAmpListGraph(ArrayList<Float> soundAmpList) {
        for(int i=0; i<soundAmpList.size(); i++) {
            xVal.add(new Entry(soundAmpList.get(i), i+1));
        }
        setXcomp.notifyDataSetChanged();
        linechart.notifyDataSetChanged();
        linechart.invalidate();
    }

    public void sttRmsListGraph(ArrayList<Float> sttRmsList) {
        for(int i=0; i<sttRmsList.size(); i++) {
            xVal2.add(new Entry(sttRmsList.get(i), i+1));
        }
        setXcomp2.notifyDataSetChanged();
        linechart.notifyDataSetChanged();
        linechart.invalidate();
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
                    "numi " + "INTEGER NOT NULL," +
                    "savedate " + "TEXT NOT NULL," +
                    "AmpList " + "TEXT NOT NULL," +
                    "RmsList " + "TEXT NOT NULL," +
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
            indexi = 0;
            xAxis = new ArrayList<>();

            // 쿼리 실행
            cursor = sqliteDB.rawQuery(sqlQueryTbl, null);
            cursor.moveToFirst();

            while (cursor.moveToNext()) {
                // 레코드가 존재한다면,

                // 값 가져오기.
                int numi = cursor.getInt(0);
                String date = cursor.getString(1);
                String AmpList = cursor.getString(2);
                String RmsList = cursor.getString(3);
                String sentenceC = cursor.getString(4);

                xAxis.add(date);
                BarEntry newdata = new BarEntry(Float.parseFloat(sentenceC), indexi); // (value, x index)
                valueSet.add(newdata);
                indexi++;
            }

            numi = indexi;

            cursor.close();

            BarDataSet barDataSet = new BarDataSet(valueSet, "");
            barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            dataSets = new ArrayList<>();
            dataSets.add(barDataSet);
        }
    }

    private void save_values(ArrayList<Float> soundAmpList, ArrayList<Float> sttRmsList, String sentenceConsistency) {
        //TODO: 수행 결과를 데이터베이스에 추가하기.

        if (sqliteDB != null) {

            // delete
            //sqliteDB.execSQL("DELETE FROM CONTACT_T") ;

            long now = System.currentTimeMillis();
            Date date = new Date(now);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String savedate = sdf.format(date);

            // 임시로 임의 값을 string화 해서 저장
            // TODO: 실제 음성 값을 처리할 것

            String AmpList = TextUtils.join(" ", soundAmpList);

            String RmsList = TextUtils.join(" ", sttRmsList);

            String sentenceC = sentenceConsistency;

            String sqlInsert = "INSERT INTO CONTACT_T " +
                    "(numi,savedate, AmpList, RmsList, sentenceC) VALUES (" +
                    Integer.toString(numi) + "," +
                    "'" + savedate + "'," +
                    "'" + AmpList + "'," +
                    "'" + RmsList + "'," +
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

        xVal2 = new ArrayList<Entry>();
        setXcomp2 = new LineDataSet(xVal2, "X");

        // 그래프 선 관련 옵션
        setXcomp.setColor(Color.BLUE);
        setXcomp.setDrawValues(false);
        setXcomp.setDrawCircles(false);
        setXcomp.setAxisDependency(YAxis.AxisDependency.LEFT);
        setXcomp.setDrawCubic(true);

        // 그래프 선 관련 옵션
        setXcomp2.setColor(Color.RED);
        setXcomp2.setDrawValues(false);
        setXcomp2.setDrawCircles(false);
        setXcomp2.setAxisDependency(YAxis.AxisDependency.LEFT);
        setXcomp2.setDrawCubic(true);

        lineDataSets = new ArrayList<ILineDataSet>();
        lineDataSets.add(setXcomp);
        lineDataSets.add(setXcomp2);

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
        barchart.moveViewToX(indexi); // set the left edge of the chart to x-index 10
                                        // moveViewToX(...) also calls invalidate()
        barchart.animateXY(1000, 1000);
        barchart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("VAL SELECTED",
                "xindex: " + e.getXIndex() + "value:" + e.getVal() + "date : " + e.getData());


        if (sqliteDB != null) {
            String sqlQueryTbl = "SELECT * FROM CONTACT_T" + " WHERE numi =" + e.getXIndex();

            Cursor cursor = null;

            // 쿼리 실행
            cursor = sqliteDB.rawQuery(sqlQueryTbl, null);
            if(cursor.moveToFirst()){
                int numi = cursor.getInt(0);
                String savedate = cursor.getString(1);
                String AmpList = cursor.getString(2);
                String RmsList = cursor.getString(3);
                String sentenceC = cursor.getString(4);

                linechart.clearValues();
                linechart.invalidate();
                chartInit();

                LoadAmpGraph(AmpList);
                LoadRmsGraph(RmsList);

                sentencediffv.setText(sentenceC +"%");
            }
            cursor.close();

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
