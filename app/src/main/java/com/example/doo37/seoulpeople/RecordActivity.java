package com.example.doo37.seoulpeople;

import android.media.MediaPlayer;
import android.speech.SpeechRecognizer;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.w3c.dom.Text;

public class RecordActivity extends AppCompatActivity {

    private TextView tv_stt;
    private TextView tv_sentence;
    private Intent i_speech;
    private ImageView bt_record;
    private SpeechRecognizer sr;
    private MediaPlayer mr;
    private InputStream is_txt;
    ArrayList<String> r_sentences = new ArrayList<>();      // 저장 된 녹음 문장 배열
    ArrayList<String> sentences = new ArrayList<>();     // 문장 파일의 문장 배열

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // TextView, Button 객체 연동
        tv_stt = (TextView) findViewById(R.id.tv_stt);
        tv_sentence = (TextView) findViewById(R.id.tv_sentence);

        bt_record = (ImageView) findViewById(R.id.bt_record);
        // At unexpected error occured, I dont know where this buttion should be.

        // 음성인식을 위한 Intent 설정
        i_speech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i_speech.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i_speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        // SpeechRecognizer 설정
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(recognitionListener);

    }

    // 녹음 버튼 클릭 이벤트
    public void recordListener(View v) {

        // 텍스트 호출
        String v_txt = "";
        try {
            is_txt = getResources().openRawResource(R.raw.txt1);
            byte[] b = new byte[is_txt.available()];
            is_txt.read(b);
            v_txt = new String(b);
        } catch (Exception e) {

        }

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
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
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

            for(int i=0; i<s_sentence.size(); i++) {
                r_sentences.add(s_sentence.get(i));
                tv_stt.setText(s_sentence.get(i) + " ");
            }

            r_sentences.add(" / ");
            bt_record.setEnabled(true);
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }

    };
}
