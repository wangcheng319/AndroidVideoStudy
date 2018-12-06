//
// Created by wangc on 2018/12/6.
//

#ifndef ANDROIDVIDEOSTUDY_WQUEUE_H
#define ANDROIDVIDEOSTUDY_WQUEUE_H

#include "queue"
#include "pthread.h"
#include "Wstatus.h"

#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wangc",FORMAT,##__VA_ARGS__);

extern "C"{
#include "libavcodec/avcodec.h"
};


class Wqueue {

public:
    std::queue<AVPacket *> queuePacket;
    pthread_mutex_t mutexPacket;
    pthread_cond_t condPacket;
    Wstatus *playStatus = NULL;

    Wqueue(Wstatus *playStatus);
    ~Wqueue();

    int putAvPacket(AVPacket *avPacket);
    int getAvPacket(AVPacket *avPacket);
    int getQueueSize();

};


#endif //ANDROIDVIDEOSTUDY_WQUEUE_H
