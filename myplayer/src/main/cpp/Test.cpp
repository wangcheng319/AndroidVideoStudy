//
// Created by wangc on 2018/11/20.
//

#include "Test.h"
#include<android/bitmap.h>
#include <aaudio/AAudio.h>
#include "string"
#include "cmath"
#include "AndroidLog.h"
#include <deque>
#include <vector>
#include <android/asset_manager.h>

/*引入ffmpeg的头文件大多需要这样，因为是C语言编写*/
extern "C" {
#include "libavcodec/avcodec.h"
}

#include <iostream>
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wangc",FORMAT,##__VA_ARGS__);
using namespace std;

Test::Test() {

}

Test::Test(int a) {

}

 void  Test::say() {
    LOGE("say");
}

void Test::say1() {
    LOGE("say1");
}

/**
 * c调用java中的方法利用的是反射
 * 1、得到字节码
 * 2、实例化该类
 * 3、获取要调用的方法
 * 4、调用方法
 * 5、调用静态方法不用实例化对象
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_wangc_myplayer_Demo_callJava(JNIEnv *env, jobject instance) {

    //1、得到字节码
    jclass jclass1 = (*env).FindClass("com/wangc/myplayer/Demo");
    //2、实例化该类
    jobject jobject1 = (*env).AllocObject(jclass1);
    //3、获取方法
    jmethodID jmethodID1 = (*env).GetMethodID(jclass1, "javaMethod", "()V");//()v为方法签名
    //4、调用方法
    (*env).CallVoidMethod(jobject1, jmethodID1);


    //调用静态方法
    env->CallStaticVoidMethod(jclass1, env->GetStaticMethodID(jclass1, "staticMethod", "()V"));
}



/**
 * 获取java传递来的对象数据
 * 参考：https://blog.csdn.net/lintax/article/details/51759270
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_wangc_myplayer_Demo_setUser(JNIEnv *env, jobject instance, jobject user) {

    //获取user中对象的class对象
    jclass clazz = env->GetObjectClass(user);
    //获取Java中的字段的id(最后一个参数是字段的签名)
    //获取String值
    jfieldID nameP = env->GetFieldID(clazz, "name", "Ljava/lang/String;");
    jstring name = (jstring) (env->GetObjectField(user, nameP));
    const char *nameR = (char *) env->GetStringUTFChars(name, 0);
    LOGE("name=%s", nameR)
    //获取int值
    jfieldID ageP = env->GetFieldID(clazz, "age", "I");
    jint age = env->GetIntField(user, ageP);
    LOGE("age=%d", age);
    //获取Boolean值
    jfieldID sexP = env->GetFieldID(clazz, "sex", "Z");
    jboolean sex = env->GetBooleanField(user, sexP);
    LOGE("sex=%d", sex)
    //获取double值
    jfieldID heightP = env->GetFieldID(clazz, "height", "D");
    jdouble height = env->GetDoubleField(user, heightP);
    LOGE("height=%lf", height);
    //获取数组
    jfieldID arryP = env->GetFieldID(clazz, "scores", "[I");
    jintArray array1 = (jintArray) env->GetObjectField(user, arryP);
    int *intArr = (int *) env->GetIntArrayElements(array1, 0);

}


