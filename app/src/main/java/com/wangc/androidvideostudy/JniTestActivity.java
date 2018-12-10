package com.wangc.androidvideostudy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.wangc.myplayer.Demo;
import com.wangc.myplayer.User;

import java.util.ArrayList;
import java.util.List;

/**
 * JNI
 */
public class JniTestActivity extends AppCompatActivity implements View.OnClickListener {
    private Demo demo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jni_test);

        demo = new Demo();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_java_call_native:
                int[] a = {23,33};
                User user = new User();
                user.age = 3;
                user.name = "zhangsan";
                user.height = 13;
                user.scores = a;


                demo.setUser(user);
                break;
            case R.id.btn_native_call_java:
                demo.callJava();
                break;
            case R.id.btn_cpp_thread:
                break;
            case R.id.btn_tran_string:
                String s = demo.setString("hello");
                Log.e("+++","返回："+s);
                break;
            case R.id.btn_native_tran:
                Log.e("+++","get user："+ demo.getUser().name);
                break;
        }
    }
}
