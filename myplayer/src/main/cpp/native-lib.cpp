#include <jni.h>
#include <string>
#include <android/log.h>

#include "pthread.h"
#include "WFFmpeg.h"
#include "Test.h"
#include <deque>
#include <vector>
#include <android/bitmap.h>

#include "iostream"
#include "PcmToMp3Test.h"


#define RGB565_R(p) ((((p) & 0xF800) >> 11) << 3)
#define RGB565_G(p) ((((p) & 0x7E0 ) >> 5)  << 2)
#define RGB565_B(p) ( ((p) & 0x1F  )        << 3)
#define MAKE_RGB565(r,g,b) ((((r) >> 3) << 11) | (((g) >> 2) << 5) | ((b) >> 3))

#define RGBA_A(p) (((p) & 0xFF000000) >> 24)
#define RGBA_R(p) (((p) & 0x00FF0000) >> 16)
#define RGBA_G(p) (((p) & 0x0000FF00) >>  8)
#define RGBA_B(p)  ((p) & 0x000000FF)
#define MAKE_RGBA(r,g,b,a) (((a) << 24) | ((r) << 16) | ((g) << 8) | (b))


using namespace std;

extern "C"
{
#include <libavformat/avformat.h>
}


#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,"wangc",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wangc",FORMAT,##__VA_ARGS__);


_JavaVM *javaVM = NULL;
WCallJava *callJava = NULL;
WFFmpeg *fFmpeg = NULL;
PcmToMp3Test *mp3Test = NULL;



/**
 * c++将图片变为灰色
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_wangc_androidvideostudy_Camera2Activity_tranBitmap(JNIEnv *env, jobject instance,
                                                            jobject bitmap) {
    AndroidBitmapInfo androidBitmapInfo;
    int ret = -1;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &androidBitmapInfo)) < 0) {
        LOGE("result=%d", ret);
        return;
    }
    int format = androidBitmapInfo.format;
    LOGE("format=%d", format);

    void * pixels = NULL;
    int res = AndroidBitmap_lockPixels(env, bitmap, &pixels);
    LOGE("res=%d", res);
    LOGE("height=%d", androidBitmapInfo.height);
    LOGE("width=%d", androidBitmapInfo.width);

    int x = 0, y = 0;
    // From top to bottom
    for (y = 0; y < androidBitmapInfo.height; ++y) {
        // From left to right
        for (x = 0; x < androidBitmapInfo.width; ++x) {
            int a = 0, r = 0, g = 0, b = 0;
            void *pixel = NULL;
            // Get each pixel by format
            if (androidBitmapInfo.format == ANDROID_BITMAP_FORMAT_RGB_565) {
                pixel = ((uint16_t *) pixels) + y * androidBitmapInfo.width + x;
                uint16_t v = *(uint16_t *) pixel;
                r = RGB565_R(v);
                g = RGB565_G(v);
                b = RGB565_B(v);
            } else {// RGBA
                pixel = ((uint32_t *) pixels) + y * androidBitmapInfo.width + x;
                uint32_t v = *(uint32_t *) pixel;
                a = RGBA_A(v);
                r = RGBA_R(v);
                g = RGBA_G(v);
                b = RGBA_B(v);
            }

            // Grayscale
            int gray = (r * 38 + g * 75 + b * 15) >> 7;

            // Write the pixel back
            if (androidBitmapInfo.format == ANDROID_BITMAP_FORMAT_RGB_565) {
                *((uint16_t *) pixel) = MAKE_RGB565(gray, gray, gray);
            } else {// RGBA
                *((uint32_t *) pixel) = MAKE_RGBA(gray, gray, gray, a);
            }
        }
    }
    AndroidBitmap_unlockPixels(env, bitmap);
}






int flush_encoder(AVFormatContext *fmt_ctx,unsigned int stream_index){
    int ret;
    int got_frame;
    AVPacket enc_pkt;
    if (!(fmt_ctx->streams[stream_index]->codec->codec->capabilities &
          CODEC_CAP_DELAY))
        return 0;
    while (1) {
        enc_pkt.data = NULL;
        enc_pkt.size = 0;
        av_init_packet(&enc_pkt);
        ret = avcodec_encode_audio2 (fmt_ctx->streams[stream_index]->codec, &enc_pkt,
                                     NULL, &got_frame);
        av_frame_free(NULL);
        if (ret < 0)
            break;
        if (!got_frame){
            ret=0;
            break;
        }
        printf("Flush Encoder: Succeed to encode 1 frame!\tsize:%5d\n",enc_pkt.size);
        /* mux encoded frame */
        ret = av_write_frame(fmt_ctx, &enc_pkt);
        if (ret < 0)
            break;
    }
    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_wangc_myplayer_Demo_PcmToMp3(JNIEnv *env, jobject instance) {

    AVFormatContext* pFormatCtx;
    AVOutputFormat* fmt;
    AVStream* audio_st;
    AVCodecContext* pCodecCtx;
    AVCodec* pCodec;

    uint8_t* frame_buf;
    AVFrame* pFrame;
    AVPacket pkt;

    int got_frame=0;
    int ret=0;
    int size=0;

    FILE *in_file=NULL;	                        //Raw PCM data
    int framenum=1000;                          //Audio frame number
    const char* out_file = "/storage/sdcard0/test.aac";          //Output URL
    int i;

//    in_file= fopen("tdjm.pcm", "rb");
    in_file = fopen("/storage/sdcard0/test.pcm","w+");

    av_register_all();

    //Method 1.
    pFormatCtx = avformat_alloc_context();
    fmt = av_guess_format(NULL, out_file, NULL);
    pFormatCtx->oformat = fmt;


    //Method 2.
    //avformat_alloc_output_context2(&pFormatCtx, NULL, NULL, out_file);
    //fmt = pFormatCtx->oformat;

    //Open output URL
    if (avio_open(&pFormatCtx->pb,out_file, AVIO_FLAG_READ_WRITE) < 0){
        printf("Failed to open output file!\n");
        return ;
    }

    audio_st = avformat_new_stream(pFormatCtx, 0);
    if (audio_st==NULL){
        return ;
    }
    pCodecCtx = audio_st->codec;
    pCodecCtx->codec_id = fmt->audio_codec;
    pCodecCtx->codec_type = AVMEDIA_TYPE_AUDIO;
    pCodecCtx->sample_fmt = AV_SAMPLE_FMT_S16;
    pCodecCtx->sample_rate= 44100;
    pCodecCtx->channel_layout=AV_CH_LAYOUT_STEREO;
    pCodecCtx->channels = av_get_channel_layout_nb_channels(pCodecCtx->channel_layout);
    pCodecCtx->bit_rate = 64000;

    //Show some information
    av_dump_format(pFormatCtx, 0, out_file, 1);

    pCodec = avcodec_find_encoder(pCodecCtx->codec_id);
    if (!pCodec){
        printf("Can not find encoder!\n");
        return ;
    }
    if (avcodec_open2(pCodecCtx, pCodec,NULL) < 0){
        printf("Failed to open encoder!\n");
        return ;
    }
    pFrame = av_frame_alloc();
    pFrame->nb_samples= pCodecCtx->frame_size;
    pFrame->format= pCodecCtx->sample_fmt;

    size = av_samples_get_buffer_size(NULL, pCodecCtx->channels,pCodecCtx->frame_size,pCodecCtx->sample_fmt, 1);
    frame_buf = (uint8_t *)av_malloc(size);
    avcodec_fill_audio_frame(pFrame, pCodecCtx->channels, pCodecCtx->sample_fmt,(const uint8_t*)frame_buf, size, 1);

    //Write Header
    avformat_write_header(pFormatCtx,NULL);

    av_new_packet(&pkt,size);

    for (i=0; i<framenum; i++){
        //Read PCM
        if (fread(frame_buf, 1, size, in_file) <= 0){
            printf("Failed to read raw data! \n");
            return ;
        }else if(feof(in_file)){
            break;
        }
        pFrame->data[0] = frame_buf;  //PCM Data

        pFrame->pts=i*100;
        got_frame=0;
        //Encode
        ret = avcodec_encode_audio2(pCodecCtx, &pkt,pFrame, &got_frame);
        if(ret < 0){
            printf("Failed to encode!\n");
            return ;
        }
        if (got_frame==1){
            printf("Succeed to encode 1 frame! \tsize:%5d\n",pkt.size);
            pkt.stream_index = audio_st->index;
            ret = av_write_frame(pFormatCtx, &pkt);
            av_free_packet(&pkt);
        }
    }

    //Flush Encoder
    ret = flush_encoder(pFormatCtx,0);
    if (ret < 0) {
        printf("Flushing encoder failed\n");
        return ;
    }

    //Write Trailer
    av_write_trailer(pFormatCtx);

    //Clean
    if (audio_st){
        avcodec_close(audio_st->codec);
        av_free(pFrame);
        av_free(frame_buf);
    }
    avio_close(pFormatCtx->pb);
    avformat_free_context(pFormatCtx);

    fclose(in_file);

}