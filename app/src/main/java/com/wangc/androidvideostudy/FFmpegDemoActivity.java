package com.wangc.androidvideostudy;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.wangc.myplayer.MyPlayer;
import com.wangc.myplayer.OnPrepareListener;

public class FFmpegDemoActivity extends AppCompatActivity implements View.OnClickListener {
    private MyPlayer myPlayer;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_demo);

        path = Environment.getExternalStorageDirectory()+"/test.mp3";
        myPlayer = new MyPlayer();
        myPlayer.setSource("http://www.ytmp3.cn/down/55494.mp3");
        myPlayer.setOnPrepareListener(new OnPrepareListener() {
            @Override
            public void onPrepared() {
                Log.e("+++","onPrepared");
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_prepare:
                myPlayer.prepare();
                break;
        }
    }
}
