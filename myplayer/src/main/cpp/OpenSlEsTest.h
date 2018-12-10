//
// Created by wangcheng on 2018/12/9.
//

#ifndef ANDROIDVIDEOSTUDY_OPENSLESTEST_H
#define ANDROIDVIDEOSTUDY_OPENSLESTEST_H


#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <string>

using namespace std;

#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wangc",FORMAT,##__VA_ARGS__);

/**
 * OpenSl es 播放pcm
 */
class OpenSlEsTest {

private:

    SLObjectItf engineObject;
    SLEngineItf engineEngine;

    SLObjectItf outputMixObject;
    SLObjectItf playerObject;
    SLPlayItf playerPlay;
    SLVolumeItf playerVolume;




public:

    OpenSlEsTest(string url);
    ~OpenSlEsTest();

    string url ;
    void beginPlay();


};


#endif //ANDROIDVIDEOSTUDY_OPENSLESTEST_H
