package com.wangc.myplayer;

import android.text.TextUtils;
import android.util.Log;

/**
 * @ProjectName: AndroidVideoStudy
 * @Package: com.wangc.myplayer
 * @ClassName: player
 * @Description: java类作用描述
 * @Author: wangc
 * @CreateDate: 2018/12/5 14:21
 * @Version: 1.0
 */
public class MyPlayer {

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("avcodec");
        System.loadLibrary("avfilter");
        System.loadLibrary("avformat");
        System.loadLibrary("avutil");
        System.loadLibrary("swresample");
        System.loadLibrary("swscale");
    }

    private String source;
    private OnPrepareListener onPrepareListener;

    public void setOnPrepareListener(OnPrepareListener onPrepareListener) {
        this.onPrepareListener = onPrepareListener;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void prepare(){
        if (TextUtils.isEmpty(source)){
            Log.e("+++","source is empty");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                n_prepare(source);
            }
        }).start();
    }

    public void onCallPrepare(){
        if (onPrepareListener != null){
            onPrepareListener.onPrepared();
        }
    }

    public native void n_prepare(String source);
}
