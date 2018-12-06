//
// Created by wangc on 2018/11/16.
//

#ifndef MYMUSIC_WAUDIO_H
#define MYMUSIC_WAUDIO_H

#include "Wqueue.h"
#include "Wstatus.h"

extern "C"
{
#include "libavcodec/avcodec.h"
};

#include "cmath"

class WAudio {

public:
    int streamIndex = -1;
    AVCodecContext *avCodecContext = NULL;
    AVCodecParameters *codecpar = NULL;
    Wqueue *wqueue = NULL;
    Wstatus *wstatus = NULL;



public:
    WAudio(Wstatus *wstatus);
    ~WAudio();

};


#endif //MYMUSIC_WAUDIO_H
