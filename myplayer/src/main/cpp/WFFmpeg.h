//
// Created by wangc on 2018/11/16.
//

#ifndef MYMUSIC_WFFMPEG_H
#define MYMUSIC_WFFMPEG_H

#include "WCallJava.h"
#include "WAudio.h"

#include "pthread.h"

extern "C"
{
#include "libavformat/avformat.h"
#include <libavcodec/avcodec.h>
};



class WFFmpeg {

public:
    WCallJava *callJava = NULL;
    const char* url = NULL;
    pthread_t decodeThread;
    AVFormatContext *pFormatCtx = NULL;
    WAudio *audio = NULL;
    Wstatus *wstatus;



public:
    WFFmpeg(WCallJava *callJava,const char* url,Wstatus *wstatus);
    ~WFFmpeg();

    void parpared();
    void decodeFFmpegThread();
    void start();


};


#endif //MYMUSIC_WFFMPEG_H
