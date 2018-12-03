package com.wangc.androidvideostudy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wangc.myplayer.Demo;

/**
 * JNI
 */
public class JniTestActivity extends AppCompatActivity {
    private Demo demo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jni_test);

        demo = new Demo();
        demo.callJava();
    }
}
