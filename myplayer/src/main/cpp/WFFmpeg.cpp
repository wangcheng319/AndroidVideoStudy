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

    //注册解码器
    av_register_all();
    //初始化网络
    avformat_network_init();
    //获取上下文
    pFormatCtx = avformat_alloc_context();

    //打开文件
    if (avformat_open_input(&pFormatCtx,url,NULL,NULL) != 0){

        LOGE("can not open url%s",url);
        LOGE("失败代码：%d",avformat_open_input(&pFormatCtx,url,NULL,NULL));
        return;
    }

    //获取音视频关键信息，比如宽高
    if (avformat_find_stream_info(pFormatCtx,NULL)<0){
        LOGE("can not find strem from url%s",url);
        return;
    }

    //判断流的类型,这里判断是音频
    for (int i = 0; i < pFormatCtx->nb_streams ; i++) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO){
            if (audio == NULL){
                audio = new WAudio();
                audio->streamIndex = i;
                audio->codecpar = pFormatCtx->streams[i]->codecpar;
            }
        }
    }


    //获取解码器
    AVCodec *avCodec = avcodec_find_decoder(audio->codecpar->codec_id);
    if (!avCodec){
        LOGE("can not find decoder from url%s",url);
        return;
    }

    //获取上下文
    audio->avCodecContext = avcodec_alloc_context3(avCodec);
    if (!audio->avCodecContext){
        LOGE("can not alloc context");
        return;
    }

    //设置上下文
    if (avcodec_parameters_to_context(audio->avCodecContext,audio->codecpar)<0){
        LOGE("can not fill context");
        return;
    };

    //打开解码器
    if (avcodec_open2(audio->avCodecContext,avCodec,0) != 0){
        LOGE("can not open audio strems ");
        return;
    }

    callJava->onCallPrepare(CHILD_THREAD);

}


void WFFmpeg::start() {

    if (audio == NULL) {
        if (LOG_DEBUG) {
            LOGE("audio is null");
            return;
        }
    }

    int count = 0;

    while (1) {
        AVPacket *avPacket = av_packet_alloc();
        if (av_read_frame(pFormatCtx, avPacket) == 0) {
            if (avPacket->stream_index == audio->streamIndex) {
                //解码操作
                count++;
                if (LOG_DEBUG) {
                    LOGE("解码第 %d 帧", count);
                }
                av_packet_free(&avPacket);
                av_free(avPacket);

            } else {
                av_packet_free(&avPacket);
                av_free(avPacket);
            }
        } else {
            if (LOG_DEBUG) {
                LOGE("decode finished");
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            break;
        }
    }
}
