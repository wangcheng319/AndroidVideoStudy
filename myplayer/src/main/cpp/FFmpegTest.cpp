//
// Created by wangc on 2018/12/5.
//

#include "FFmpegTest.h"
#include "AndroidLog.h"
#include <string>
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
