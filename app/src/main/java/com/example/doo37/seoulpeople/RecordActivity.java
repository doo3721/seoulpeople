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
    private InputStream is_txt;
    ArrayList<String> r_sentences = new ArrayList<>();      // 저장 된 녹음 문장 배열
    ArrayList<String> t_sentences = new ArrayList<>();     // 문장 파일의 문장 배열
    ArrayList<Float> sttRmsList = new ArrayList<>();           // 음성 rms수치 리스트
    ArrayList<Float> soundAmpList = new ArrayList<>();          // 음성파일 amp수치 리스트
    float changedRMS = -100;

    // soundAmpList와 sttRmsList의 크기를 같게 해주는 요소
    int soundCount = 0;                                 // soundAmpList의 크기를 결정하는 count
    int sttCount = 0;                                   // soundAmpList의 크기를 토대로 sttRmsList의 크기를 결정
    boolean isSTTReady = false;                       // STT 준비 상태
    int readyCount = 0;                                // 준비와 시작 상태 사이의 대기 카운트
    boolean isSTTStart = false;                       // STT 시작 상태

    private boolean isEndOfSpeech = false;

    MyThread thread = new MyThread();
    private DetectNoise mSensor;

    // 차트 관련 선언
    LineChart chart;
    int RANGE = 100;
    // int DATA_RANGE =100;

    ArrayList<Entry> xVal;
    ArrayList<Entry> xVal2;
    LineDataSet setXcomp;
    LineDataSet setXcomp2;
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
                mSensor.stop();

                // rms stop
                sr.stopListening();
                finish();
                //키 값으로 String이라는 이름을 지정하며 , 두번째 인자로 전송할 데이터 변수 지정   .putExtra("key",value);
                Intent intent = new Intent(getApplicationContext(),ResultActivity.class);   //첫번째 인자 나의 클래스명, 두번째 인자 이동할 클래스명

                //키 값으로 ArrayList라는 이름으로 지정, 전송할 데이터 변수 지정 .putStringArrayListExtra("KEY",value);
                //intent.putStringArrayListExtra("ArrayList", ArrData);

                //IntentPage Activity에 데이터를 전달.

                startActivity(intent);  //인텐트를 시작한다.
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
            }
        });

        init(); // 차트 생성 및 옵션 부여

        // 음성인식을 위한 Intent 설정
        i_speech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i_speech.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        i_speech.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i_speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mSensor = new DetectNoise();

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

        // txt, voice를 저장할 것
        int txt = R.raw.txt1;
        int voice = R.raw.voice1;

        // 텍스트 호출
        try {
            is_txt = getResources().openRawResource(txt);
            byte[] b = new byte[is_txt.available()];
            is_txt.read(b);
            v_txt = new String(b);
        } catch (Exception e) {

        }

        t_sentences.add(v_txt);
        tv_sentence.setText(v_txt);
        bt_record.setEnabled(false);

        threadStart(); // 차트에 데이터 넣을 스레드 실행
        mSensor.start();

        // mediaplayer 설정
        mr = MediaPlayer.create(this, voice);
        mr.start();

        mr.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();

                threadStop();
                mSensor.stop();

                threadRestart();

                // stt 호출
                sr.startListening(i_speech);
            }
        });
    }

    // 다시 버튼 클릭 이벤트
    public void replayListener(View v) {
        mSensor.stop();
        finish();
        // rms stop
        sr.stopListening();
        startActivity(new Intent(this, RecordActivity.class));
        this.overridePendingTransition(0, 0);
    }

    // SpeechRecognizer의 이벤트 메소드 설정
    private RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            isSTTReady = true;
            Log.d("메세지: ", "준비");
        }

        @Override
        public void onBeginningOfSpeech() {
            isSTTStart = true;
            Log.d("메세지: ", "시작");
        }

        @Override
        public void onRmsChanged(float v) {
            float rms = -100;

            // STT 시작전 대기
            if(isSTTReady && !isSTTStart) {
                if (readyCount > 5) {
                    rms = 0;
                    ++sttCount;
                    sttRmsList.add(rms);
                }
                else {
                    readyCount++;
                }
            }

            // STT 시작
            if(isSTTStart && (sttCount < soundCount)) {
                rms = (float) ((v - (-2.0)) / (10.0 - (-2.0)) * 100.0);
                if (rms <= 0) rms = 0.0f;   if(rms >= 100) rms = 100.0f;
                ++sttCount;
                sttRmsList.add(rms);
            }

            changedRMS = rms;
            Log.d("R-RMS:", " " + rms + ", 카운트: " + String.valueOf(sttCount));
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
            isEndOfSpeech = true;
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

            ArrayList<String> s_sentence = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);

            // 0번이 가장 신뢰도 높은 결과
            r_sentences.add(s_sentence.get(0));

            // rms stop
            sr.stopListening();

            r_sentences.add(" / ");

            tv_stt.setText(s_sentence.get(0));
            bt_record.setEnabled(true);

            // 두 텍스트 비교 알고리즘 사용 부분
            LinkedList<diff_match_patch.Diff> diff = dmp.diff_main(s_sentence.get(0), v_txt);

            ArrayList<String> cmp_temp = new ArrayList<>();

            dmp.diff_cleanupEfficiency(diff);

            String c_txt = "";
            String c_txt2 = "";
            usrsLength = 0;
            stdsLength = 0;

            try {
                for (diff_match_patch.Diff d : diff) {
                    if (d.operation == diff_match_patch.Operation.INSERT) {
                        c_txt = c_txt + " " + d.text;
                        c_txt2= c_txt2 + d.text;
                    }
                    else if (d.operation == diff_match_patch.Operation.EQUAL) {
                        c_txt = c_txt + " " + d.text;
                        c_txt2= c_txt2 + d.text;
                        cmp_temp.add(d.text);
                        usrsLength = usrsLength + d.text.length();
                    }
                }
            } catch (Exception e) {
            }

            stdsLength = c_txt2.length() - 1;

            SpannableStringBuilder ssb = new SpannableStringBuilder(c_txt);

            for (int j = 0; j < cmp_temp.size(); j++) {
                if (c_txt.contains(cmp_temp.get(j))) {
                    int i = c_txt.indexOf(cmp_temp.get(j));
                    ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#5F00FF")), i, i + cmp_temp.get(j).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            setstdsLength(stdsLength);
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
            Log.d("문장: ", s_temp);
            if(!isEndOfSpeech)
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

        chart.getAxisRight().setAxisMinValue(0.0f);
        chart.getAxisLeft().setAxisMinValue(0.0f);
        chart.getAxisRight().setAxisMaxValue(100.0f);
        chart.getAxisLeft().setAxisMaxValue(100.0f);

        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);

        chart.getXAxis().setEnabled(false);

        //chart.getAxisRight().setEnabled(false);
        //chart.getAxisLeft().setEnabled(false);
        chart.getLegend().setEnabled(false);

        xVal = new ArrayList<Entry>();
        setXcomp = new LineDataSet(xVal, "X");
        xVal2 = new ArrayList<Entry>();
        setXcomp2 = new LineDataSet(xVal2, "X2");

        // 그래프 선 관련 옵션
        setXcomp.setColor(Color.BLUE);
        setXcomp.setDrawValues(false);
        setXcomp.setDrawCircles(true);
        setXcomp.setCircleColor(Color.BLUE);
        setXcomp.setAxisDependency(YAxis.AxisDependency.LEFT);
        setXcomp.setDrawCubic(true);
        setXcomp.setLineWidth(2); //줄 두께

        setXcomp2.setColor(Color.RED);
        setXcomp2.setDrawValues(false);
        setXcomp2.setDrawCircles(true);
        setXcomp2.setCircleColor(Color.RED);
        setXcomp2.setAxisDependency(YAxis.AxisDependency.LEFT);
        setXcomp2.setDrawCubic(true);
        setXcomp2.setLineWidth(2); //줄 두께

        lineDataSets = new ArrayList<ILineDataSet>();
        lineDataSets.add(setXcomp);
        lineDataSets.add(setXcomp2);


        xVals = new ArrayList<String>();
        for (int i = 0; i < RANGE; i++) {
            xVals.add("");
        }

        lineData = new LineData(xVals,lineDataSets);

        chart.setData(lineData);
        //chart.invalidate();
    }

    // 차트 업데이트 함수
    public void chartUpdate(float v) {

        xVal.add(new Entry(v,xVal.size()));
        setXcomp.notifyDataSetChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    public void chartUpdate2(float v) {

        xVal2.add(new Entry(v,xVal2.size()));
        setXcomp2.notifyDataSetChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    // 쓰레드 돌릴 핸들러
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    ++soundCount;
                    float amp = (float) mSensor.getAmplitude();
                    amp = (float) ((amp - (-20.0)) / (12.0 - (-20.0)) * 100.0);
                    if (amp <= 0) amp = 0.0f;   if(amp >= 100) amp = 100.0f;
                    soundAmpList.add(amp);
                    Log.d("앰프", String.valueOf(amp) + ", 카운트: "+String.valueOf(soundCount));
                    chartUpdate(amp);
                    break;
                case 1:
                    thread.stopThread();
                    break;
                case 2:
                    thread.restartThread();
                    break;
                case 3:
                    if((changedRMS >= 0.0f) && (sttCount < soundCount))
                        chartUpdate2(changedRMS);
                    break;
            }
        }
    };

    // 핸들러 상속 구문
    class MyThread extends Thread {
        boolean stopped = false;
        boolean restart = false;

        public void stopThread(){
            stopped = true;
        }
        public void restartThread(){
            restart = true;
        }

        @Override
        public void run() {
            while(stopped == false) {
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while(restart == true) {
                handler.sendEmptyMessage(3);
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
        thread.setDaemon(true);
        thread.start();
    }

    private void threadStop() {
        handler.sendEmptyMessage(1);
    }

    private void threadRestart() {
        handler.sendEmptyMessage(2);
    }
}
