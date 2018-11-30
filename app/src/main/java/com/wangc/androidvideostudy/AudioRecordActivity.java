package com.wangc.androidvideostudy;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * AudioRecord录音 pcm
 * AudioTrack播放
 */
public class AudioRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    //声音来源
    private static final int audioSource = MediaRecorder.AudioSource.MIC;
    //采样率，44100基本通用
    private static final int sampleRateInHz = 44100;
    //输入声道
    private static final int inChannelConfig =  AudioFormat.CHANNEL_IN_DEFAULT;
    //输出声道
    private static  final int outChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
    //量化精度，比特率，
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //录音
    private int bufferSizeInBytes;
    //播放
    private int bufferSize;
    //存放文件
    private static final String filePath = Environment.getExternalStorageDirectory()+"/test.pcm";

    private boolean isRecord = false;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
        
        initRecord();
        initAudioTrack();


    }

    private void initRecord() {
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,inChannelConfig,audioFormat);
        audioRecord = new AudioRecord(audioSource,sampleRateInHz,inChannelConfig,audioFormat,bufferSizeInBytes);
        Log.e("+++","bufferSizeInBytes:"+bufferSizeInBytes);
    }

    private void initAudioTrack() {
        bufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, outChannelConfig, audioFormat);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, outChannelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
        Log.e("+++","bufferSize:"+bufferSize);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startRecord();
                    }
                }).start();
                break;
            case R.id.btn_stop:
                stopRecord();
                Log.e("+++","stop");
                break;
            case R.id.btn_play:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        play();
                    }
                }).start();
                break;
        }
    }

    private void startRecord() {
        file = new File(filePath);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        audioRecord.startRecording();
        isRecord = true;

        byte[] data = new byte[bufferSizeInBytes];
        int result = -1;
        while (isRecord){
            result = audioRecord.read(data,0,bufferSizeInBytes);
            if (result!= AudioRecord.ERROR_INVALID_OPERATION){
                    try {
                        Log.e("+++","录音中："+data);
                        fileOutputStream.write(data);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }

        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void stopRecord() {
        isRecord = false;
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        Log.e("+++","录音结束");
    }

    private void play() {
        Log.e("+++","开始播放");
        try {
            File file = new File(filePath);
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            byte[] tempBuffer = new byte[bufferSize];
            int readCount = 0;
            while (dis.available() > 0) {
                readCount= dis.read(tempBuffer);
                if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                    continue;
                }
                if (readCount != 0 && readCount != -1) {
                    audioTrack.play();
                    audioTrack.write(tempBuffer, 0, readCount);
                }
            }

            try {
                if (audioTrack != null) {
                    if (audioTrack.getState() == AudioRecord.STATE_INITIALIZED) {
                        audioTrack.stop();
                    }
                    if (audioTrack != null) {
                        audioTrack.release();
                    }
                }
                if (dis != null) {
                    dis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
