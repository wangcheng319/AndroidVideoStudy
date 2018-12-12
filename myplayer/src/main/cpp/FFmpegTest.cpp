//
// Created by wangc on 2018/12/5.
//

#include "FFmpegTest.h"
#include "AndroidLog.h"
#include <string>
#include <jni.h>
#include "pthread.h"

pthread_t pthread;

void FFmpegTest::mEnCode() {

    LOGE("path=%s","测试路径");

    createThread();
}

/**
 * 线程回调执行函数
 */
void * threadCallBack(void *data){
    LOGE("path=%s","线程创建成功");
    //线程执行完要退出，不然app闪退
    pthread_exit(&pthread);
};

/**
 * 创建一个线程
 */
void FFmpegTest::createThread() {
    pthread_create(&pthread,NULL,threadCallBack,NULL);
}


/**
 * 获取音视频信息
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_wangc_myplayer_FFmpegUtils_getVideoInfo(JNIEnv *env, jclass type) {

    AVFormatContext *avFormatContext = NULL;

    LOGE("start to get vider info");

    const char* path1 = "/storage/emulated/0/wuyuetian3.mp4";

    av_register_all();

    int result = avformat_open_input(&avFormatContext,path1,NULL,NULL);

    //根据错误码获取具体的错误信息
    if (result<0){
        LOGE("cant open file:%s\n",av_err2str(result));
    }

    av_dump_format(avFormatContext,0,path1,0);

    LOGE("时长：%d",avFormatContext->duration);
    LOGE("Metadata:%s",avFormatContext->metadata);
    LOGE("video:%s",avFormatContext->video_codec);

    //找到音频流
    av_find_best_stream(avFormatContext,AVMEDIA_TYPE_AUDIO,-1,-1,NULL,0);



    avformat_close_input(&avFormatContext);



}
