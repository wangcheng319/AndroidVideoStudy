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

/**
 * jstring 转 c++  string
 */
std::string jstring2str(JNIEnv* env1, jstring jstr) {
    char*   rtn   =   NULL;

    jclass   clsstring   =   env1->FindClass("java/lang/String");
    jstring   strencode   =   env1->NewStringUTF("GB2312");
    jmethodID   mid   =   env1->GetMethodID(clsstring,   "getBytes",   "(Ljava/lang/String;)[B");
    jbyteArray   barr=   (jbyteArray)env1->CallObjectMethod(jstr,mid,strencode);
    jsize   alen   =   env1->GetArrayLength(barr);
    jbyte*   ba   =   env1->GetByteArrayElements(barr,JNI_FALSE);
    if(alen   >   0)
    {
        rtn   =   (char*)malloc(alen+1);
        memcpy(rtn,ba,alen);
        rtn[alen]=0;
    }
    env1->ReleaseByteArrayElements(barr,ba,0);
    std::string stemp(rtn);
    free(rtn);
    return   stemp;
}

/**
 * c++ string  转 char
 */
char* string2char( std::string str)
{
    std::string s1 = str;
    char *data;
    int len = s1.length();
    data = (char *)malloc((len+1)*sizeof(char));
    s1.copy(data,len,0);
    return data;
}



/**
 * 获取java传递的string，拼接后返回
 */
extern "C"
JNIEXPORT jstring JNICALL
Java_com_wangc_myplayer_Demo_setString(JNIEnv *env, jobject instance, jstring s_) {
    //得到char * 字符串
    const char *s = env->GetStringUTFChars(s_, 0);
    LOGE("接收string=%s",s);
    //将jstring转为c++ 中的string
    string string1 = jstring2str(env,s_);
    //c++  拼接字符串
    string1.append(" c++");

    LOGE("拼接后string=%s",string1.c_str());
    //将c++中的string 转为jstring
    jstring  result = env->NewStringUTF(string1.c_str());

    return result;
}


//定义与java对应的结构体，也可以不用定义，都采取直接赋值
typedef struct {
 string name;
 int age;
 bool sex;
 double height;
 int scores[];
} User;

/**
 * c++  传递对象到java
 */
extern "C"
JNIEXPORT jobject JNICALL
Java_com_wangc_myplayer_Demo_getUser(JNIEnv *env, jobject instance) {

    //创建一个对象，保存数据，方便接下来赋值给新建的对象
    User user1;
    user1.age = 3;
    user1.name = "lisi";
    user1.sex = false;
    user1.height = 100.0;
//    user1.scores = {1,2,3};

    //获取对应的类
    jclass jclass1 = env->FindClass("com/wangc/myplayer/User");
    //获取对应的字段
    jfieldID age = env->GetFieldID(jclass1,"age","I");
    jfieldID name = env->GetFieldID(jclass1,"name","Ljava/lang/String;");
    jfieldID sex = env->GetFieldID(jclass1,"sex","Z");
    jfieldID height = env->GetFieldID(jclass1,"height","D");
//    jfieldID scores = env->GetFieldID(jclass1,"scores","[I");


    //创建对象
    jobject user = env->AllocObject(jclass1);
    //为属性赋值
    env->SetIntField(user,age,user1.age);
    env->SetObjectField(user,name,env->NewStringUTF(user1.name.c_str()));
    env->SetBooleanField(user,sex,user1.sex);
    env->SetDoubleField(user,height,user1.height);


    return user;


}











