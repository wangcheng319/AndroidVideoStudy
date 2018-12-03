package com.wangc.myplayer;

import android.util.Log;

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
     * @param user
     */
    public native void setUser(User user);

    public native void PcmToMp3(String path);
}
