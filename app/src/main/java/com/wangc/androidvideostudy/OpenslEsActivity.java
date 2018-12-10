package com.wangc.androidvideostudy;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * OpenslEs 播放原始PCM音频
 */
public class OpenslEsActivity extends AppCompatActivity implements View.OnClickListener {

    static {
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opensl_es);
    }

    @Override
    public void onClick(View v) {
//        String path = Environment.getExternalStorageDirectory()+"/test.pcm";

        final String path = "mnt/sdcard/DCIM/test.pcm";
        new Thread(new Runnable() {
            @Override
            public void run() {
                playPcm(path);
                Log.e("+++","播放");
            }
        }).start();

    }

    public native void playPcm(String path);
}
