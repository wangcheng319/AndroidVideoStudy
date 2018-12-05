//
// Created by wangc on 2018/11/16.
//

#include <pthread.h>
#include <syslog.h>

#include "WFFmpeg.h"
#include "AndroidLog.h"


WFFmpeg::WFFmpeg(WCallJava *callJava, const char *url) {

    this->callJava = callJava;
    this->url = url;

}

void *decodeFFmpeg(void *data){
    WFFmpeg *wfFmpeg = (WFFmpeg*)(data);
    wfFmpeg->decodeFFmpegThread();

    pthread_exit(&wfFmpeg->decodeThread);

}

void WFFmpeg::parpared() {

    pthread_create(&decodeThread,NULL,decodeFFmpeg,this);

}

void WFFmpeg::decodeFFmpegThread() {

    av_register_all();
    avformat_network_init();
    pFormatCtx = avformat_alloc_context();

    if (avformat_open_input(&pFormatCtx,url,NULL,NULL) != 0){

        LOGE("can not open url%s",url);
        LOGE("失败代码：%d",avformat_open_input(&pFormatCtx,url,NULL,NULL));
        return;
    }

    if (avformat_find_stream_info(pFormatCtx,NULL)<0){
        LOGE("can not find strem from url%s",url);
        return;
    }

    for (int i = 0; i < pFormatCtx->nb_streams ; i++) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO){
            if (audio == NULL){
                audio = new WAudio();
                audio->streamIndex = i;
                audio->codecpar = pFormatCtx->streams[i]->codecpar;
            }
        }
    }


    AVCodec *avCodec = avcodec_find_decoder(audio->codecpar->codec_id);
    if (!avCodec){
        LOGE("can not find decoder from url%s",url);
        return;
    }

    audio->avCodecContext = avcodec_alloc_context3(avCodec);
    if (!audio->avCodecContext){
        LOGE("can not alloc context");
        return;
    }

    if (avcodec_parameters_to_context(audio->avCodecContext,audio->codecpar)<0){
        LOGE("can not fill context");
        return;
    };

    if (avcodec_open2(audio->avCodecContext,avCodec,0) != 0){
        LOGE("can not open audio strems ");
        return;
    }

    callJava->onCallPrepare(CHILD_THREAD);

}
