package com.wangc.myplayer;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: AndroidVideoStudy
 * @Package: com.wangc.myplayer
 * @ClassName: Demo
 * @Description: java类作用描述
 * @Author: wangc
 * @CreateDate: 2018/12/3 16:06
 * @Version: 1.0
 */
public class Demo {
    static {
        System.loadLibrary("native-lib");

    }

    /**
     * c中的callJava方法调用java中的javaMethod方法
     * @return
     */
    public native void callJava();


    public void javaMethod(){
        Log.e("wangc","c 调用 java方法");
    }

    public static void staticMethod(){
        Log.e("wangc","c 调用 java static方法");
    }

    /**
     * java向native传递对象
     * @param users
     */
    public native void setUser(User users);

    /**
     * 获取FFmpeg配置
     */
    public native void getFFmpegConfig();

    /**
     * java传递string到native
     */

    public native String setString(String s);

    /**
     * c++ 传递对象到java
     * @return
     */
    public native User getUser();

}
