package com.example.doo37.seoulpeople;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.w3c.dom.Text;
import java.util.Timer;
import java.util.TimerTask;

import java.util.LinkedList;
import com.example.doo37.seoulpeople.diff_match_patch;

import static com.example.doo37.seoulpeople.diff_match_patch.Operation.DELETE;
import static com.example.doo37.seoulpeople.diff_match_patch.Operation.EQUAL;
import static com.example.doo37.seoulpeople.diff_match_patch.Operation.INSERT;

public class RecordActivity extends AppCompatActivity {

    private TextView tv_stt;
    private TextView tv_sentence;
    private Intent i_speech;
    private ImageView bt_record;
    private ImageView bt_retry;
    private ImageView bt_end;
    private SpeechRecognizer sr;
    private MediaPlayer mr;
    private Visualizer vl;
    private InputStream is_txt;
    ArrayList<String> r_sentences = new ArrayList<>();      // 저장 된 녹음 문장 배열
    ArrayList<String> t_sentences = new ArrayList<>();     // 문장 파일의 문장 배열
    ArrayList<Float> rmsList = new ArrayList<>();           // 음성 rms수치 리스트
    float changedRMS;

    Timer sTimer;

    // 차트 관련 선언
    LineChart chart;
    int X_RANGE = 50;
    int DATA_RANGE = 30;

    ArrayList<Entry> xVal;
    LineDataSet setXcomp;
    ArrayList<String> xVals;
    ArrayList<ILineDataSet> lineDataSets;
    LineData lineData;

    private final int MY_AUDIOSESSION = 0;

    // 문자열 매칭 알고리즘 선언부
    diff_match_patch dmp = new diff_match_patch();

    // 비교 부분에서 사용해야 하기 때문에 v_txt 옮김
    String v_txt = "";

    // 사용자 문장 비교를 위한 변수 목록
    private TextView tv_compare;
    ArrayList<String> t_compare = new ArrayList<>();
    String c_txt = "";

    // 일치율 검사를 위한 변수 목록
    public static Context mContext;
    private int stdsLength;
    private int usrsLength;

    // 음성 일치율 검사를 위한 변수 목록
    // TODO: 음성 일치율 계산

    SpannableStringBuilder ssb = new SpannableStringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // 싱글톤 패턴 이용
        mContext = this;

        // TextView, Button 객체 연동
        tv_stt = (TextView) findViewById(R.id.tv_stt);
        tv_sentence = (TextView) findViewById(R.id.tv_sentence);

        // 비교 텍스트 출력
        tv_compare = (TextView) findViewById(R.id.tv_compare);

        bt_record = (ImageView) findViewById(R.id.bt_record);
        bt_retry = (ImageView) findViewById(R.id.bt_retry);
        bt_end = (ImageView) findViewById(R.id.bt_end);
        bt_end.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                finish();
                //키 값으로 String이라는 이름을 지정하며 , 두번째 인자로 전송할 데이터 변수 지정   .putExtra("key",value);
                Intent intent = new Intent(getApplicationContext(),ResultActivity.class);   //첫번째 인자 나의 클래스명, 두번째 인자 이동할 클래스명

                //키 값으로 ArrayList라는 이름으로 지정, 전송할 데이터 변수 지정 .putStringArrayListExtra("KEY",value);
                //intent.putStringArrayListExtra("ArrayList", ArrData);

                //IntentPage Activity에 데이터를 전달.

                startActivity(intent);  //인텐트를 시작한다.
            }
        });

        // 음성인식을 위한 Intent 설정
        i_speech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i_speech.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        i_speech.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i_speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        // 음성 파일 RMS 측정을 위한 Visualizer 초기화
        vl = new Visualizer(MY_AUDIOSESSION);
        vl.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS);
        vl.setEnabled(true);

        // 음성 파일 RMS 측정을 위한 timertask 생성
        sTimer = new Timer();
        sTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Visualizer.MeasurementPeakRms mpr = new Visualizer.MeasurementPeakRms();
                vl.getMeasurementPeakRms(mpr);
                float maxmin = (float) ((-4500.0) - (-9600.0));
                float rms = (float) ((mpr.mRms - (-9600.0)) / maxmin);
                Log.d("RMS:", " " + rms);
            }
        }, 0, 100);

        // SpeechRecognizer 설정
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(recognitionListener);

        stdsLength = 0;
        usrsLength = 0;

        init(); // 차트 생성 및 옵션 부여
    }

    public void setstdsLength(int stdsLength){
        this.stdsLength = stdsLength;
    }

    public void setusrsLength(int usrsLength){
        this.usrsLength = usrsLength;
    }

    public int getstdsLength(){
        return stdsLength;
    }

    public int getusrsLength(){
        return usrsLength;
    }

    // 녹음 버튼 클릭 이벤트
    public void recordListener(View v) {

        bt_record.setVisibility(View.GONE);
        bt_retry.setVisibility(View.VISIBLE);

        threadStart(); // 차트에 데이터 넣을 스레드 실행

        // 텍스트 호출
        try {
            is_txt = getResources().openRawResource(R.raw.txt1);
            byte[] b = new byte[is_txt.available()];
            is_txt.read(b);
            v_txt = new String(b);
        } catch (Exception e) {

        }

        t_sentences.add(v_txt);
        tv_sentence.setText(v_txt);
        bt_record.setEnabled(false);

        // mediaplayer 설정
        mr = MediaPlayer.create(this, R.raw.voice1);
        mr.start();
        mr.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
                sTimer.cancel();
                sTimer.purge();
                // stt 호출
                sr.startListening(i_speech);
            }
        });
    }

    // 다시 버튼 클릭 이벤트
    public void replayListener(View v) {
        finish();
        startActivity(new Intent(this, RecordActivity.class));
    }

    // SpeechRecognizer의 이벤트 메소드 설정
    private RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Log.d("메세지: ", "준비");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d("메세지: ", "시작");
        }

        @Override
        public void onRmsChanged(float v) {
            float rms = (float) ((v - (-2.0)) / (10.0 - (-2)));
            Log.d("RMS:", " " + rms);
            rmsList.add(v);
            changedRMS = v;
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
            Log.d("메세지: ", "종료");
        }

        // 에러 호출
        @Override
        public void onError(int error) {
            tv_stt.setText("ERROR: " + error);
        }

        // 녹음 후 결과 출력, 녹음 문장 저장
        @Override
        public void onResults(Bundle results) {
            tv_stt.setText("");
            ArrayList<String> s_sentence = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);

            // 0번이 가장 신뢰도 높은 결과
            r_sentences.add(s_sentence.get(0));
            r_sentences.add(" / ");

            tv_stt.setText(s_sentence.get(0));
            bt_record.setEnabled(true);

            // 두 텍스트 비교 알고리즘 사용 부분

            LinkedList<diff_match_patch.Diff> diff = dmp.diff_main(s_sentence.get(0), v_txt);

            ArrayList<String> cmp_temp = new ArrayList<>();

            dmp.diff_cleanupEfficiency(diff);

            String c_txt = "";
            usrsLength = 0;
            stdsLength = 0;

            try {
                for (diff_match_patch.Diff d : diff) {
                    if (d.operation == diff_match_patch.Operation.INSERT) {
                        c_txt = c_txt + " " + d.text;
                    }
                    else if (d.operation == diff_match_patch.Operation.EQUAL) {
                        c_txt = c_txt + " " + d.text;
                        cmp_temp.add(d.text);
                    }
                }
            } catch (Exception e) {
            }

            SpannableStringBuilder ssb = new SpannableStringBuilder(c_txt);

            for (int j = 0; j < cmp_temp.size(); j++) {
                if (c_txt.contains(cmp_temp.get(j))) {
                    int i = c_txt.indexOf(cmp_temp.get(j));
                    usrsLength = usrsLength + cmp_temp.get(j).length();
                    ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#5F00FF")), i, i + cmp_temp.get(j).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            setstdsLength(c_txt.length() - usrsLength);
            setusrsLength(usrsLength);

            // 문자열 비교를 위한 테스트 구문

            /*
            stdsLength = getstdsLength();
            usrsLength = getusrsLength();
            String temp = String.valueOf(usrsLength);
            Toast.makeText(getApplication(), temp, Toast.LENGTH_LONG).show();
            temp = String.valueOf(stdsLength);
            Toast.makeText(getApplication(), temp, Toast.LENGTH_LONG).show();
            */

            tv_compare.setText("");
            tv_compare.append(ssb);

            bt_end.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPartialResults(Bundle parts) {
            ArrayList<String> s_parts = (ArrayList<String>) parts.get(SpeechRecognizer.RESULTS_RECOGNITION);
            String s_temp = "";
            for (int i=0; i<s_parts.size(); i++) {
                s_temp += s_parts.get(i);
            }
            tv_stt.setText(s_temp);
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }

    };

    private void init() { // 차트 생성 함수
        chart = (LineChart) findViewById(R.id.chart);

        chartInit();
    }

    private void chartInit() {
        chart.setAutoScaleMinMaxEnabled(true);

        // chart UI 옵션
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);

        chart.setDescription("");

        // grid line 비활성화
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);

        chart.getXAxis().setEnabled(false);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getLegend().setEnabled(false);

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
                chartUpdate(changedRMS);
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
                    Thread.sleep(50);
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
        thread.start();
    }
}
