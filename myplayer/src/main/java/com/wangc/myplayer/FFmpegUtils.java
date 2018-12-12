package com.wangc.myplayer;

/**
 * FFmpeg 功能演示
 */
public class FFmpegUtils {

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("avformat-57");
    }


    public  native static void  getVideoInfo();
}
