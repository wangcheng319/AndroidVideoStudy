//
// Created by wangc on 2018/11/16.
//
/**
 * 头文件一般用于定义类的函数和变量，不做具体实现
 */

#ifndef MYMUSIC_WCALLJAVA_H
#define MYMUSIC_WCALLJAVA_H

#include "jni.h"
#include "cwchar"
#include <android/log.h>
#include "WAudio.h"

#define MAIN_THREAD  0
#define CHILD_THREAD  1


class WCallJava {

private:
    _JavaVM *javaVM = NULL;
    JNIEnv *jniEnv = NULL;
    jobject jobj;

    jmethodID jmid_parpared;

public:
    WCallJava(_JavaVM *javaVM, JNIEnv *env, jobject *obj);
    ~WCallJava();

    void onCallPrepare(int type);

    void parpared();
    void decodeFFmpegThread();
    void start();

};


#endif //MYMUSIC_WCALLJAVA_H
