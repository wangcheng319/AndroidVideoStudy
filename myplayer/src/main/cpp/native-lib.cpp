#include <jni.h>
#include <string>
#include <android/log.h>

#include "pthread.h"
#include "WFFmpeg.h"
#include "WCallJava.h"
#include "Test.h"
#include <deque>
#include <vector>
#include <android/bitmap.h>

#include "iostream"
#include "PcmToMp3Test.h"
#include "FFmpegTest.h"



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
Wstatus *wstatus = NULL;



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



/**
 * 获取FFmpeg配置
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_wangc_myplayer_Demo_getFFmpegConfig(JNIEnv *env, jobject instance) {

    LOGE("配置=%s", avcodec_configuration())

    FFmpegTest fFmpegTest;
    fFmpegTest.mEnCode();
}



extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jint result = -1;
    javaVM = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {

        return result;
    }
    return JNI_VERSION_1_4;

}


extern "C"
JNIEXPORT void JNICALL
Java_com_wangc_myplayer_MyPlayer_n_1prepare(JNIEnv *env, jobject instance, jstring source_) {
    const char *source = env->GetStringUTFChars(source_, 0);

    if (fFmpeg == NULL){
        if (callJava == NULL){
            callJava = new WCallJava(javaVM,env,&instance);
        }
    }

    wstatus = new Wstatus();
    fFmpeg = new WFFmpeg(callJava,source,wstatus);
    fFmpeg->parpared();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_wangc_myplayer_MyPlayer_n_1start(JNIEnv *env, jobject instance) {

    if (fFmpeg != NULL) {
        fFmpeg->start();
    }

}

/**
 * opsles播放pcm数据
 */
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
SLObjectItf  engineObject = NULL;
SLEngineItf  engineEngine =NULL;
SLObjectItf  outputMixObjext = NULL;
SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;
SLObjectItf  pcmPlayer = NULL;
SLPlayItf  pcmPlayerItf = NULL;
SLAndroidSimpleBufferQueueItf  pcmBufferQueue = NULL;

FILE *pcmFile;
void *buffer;
uint8_t  *out_buffer;


int getPcmData(void **pcm){
    int size = 0;
    while (!feof(pcmFile)){
        size = fread(out_buffer,1,44100*2*2,pcmFile);
        if (out_buffer == NULL){
            LOGE("读取数据完毕");
            break;
        } else{
            LOGE("读取中……");
        }

        *pcm = out_buffer;
        break;
    }

    return size ;
}

void pcmBufferCallBack(SLAndroidSimpleBufferQueueItf bufferQueueItf,void *context){
    int size = getPcmData(&buffer);
    if (buffer != NULL){
        (*pcmBufferQueue)->Enqueue(pcmBufferQueue,buffer,size);
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_wangc_androidvideostudy_OpenslEsActivity_playPcm(JNIEnv *env, jobject instance,
                                                          jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);

    pcmFile = fopen(path,"r");
    if (pcmFile == NULL){
        LOGE("打开PCM文件失败");
        return;
    }

    out_buffer = static_cast<uint8_t *>(malloc(44100 * 2 * 2));


    slCreateEngine(&engineObject,0,0,0,0,0);
    (*engineObject)->Realize(engineObject,SL_BOOLEAN_FALSE);
    (*engineObject)->GetInterface(engineObject,SL_IID_ENGINE,&engineEngine);

    const SLInterfaceID  mids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean  mreq[1] = {SL_BOOLEAN_FALSE};

    (*engineEngine)->CreateOutputMix(engineEngine,&outputMixObjext,1,mids,mreq);
    (*outputMixObjext)->Realize(outputMixObjext,SL_BOOLEAN_FALSE);
    (*outputMixObjext)->GetInterface(outputMixObjext,SL_BOOLEAN_FALSE,&outputMixEnvironmentalReverb);


    (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(outputMixEnvironmentalReverb,&reverbSettings);

    //创建播放器

    SLDataLocator_AndroidBufferQueue androidBufferQueue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,2};
    SLDataFormat_PCM pcm = {SL_DATAFORMAT_PCM,
                            2,
                            SL_SAMPLINGRATE_44_1,
                            SL_PCMSAMPLEFORMAT_FIXED_16,
                            SL_PCMSAMPLEFORMAT_FIXED_16,
                            SL_SPEAKER_FRONT_LEFT|SL_SPEAKER_FRONT_RIGHT,
                            SL_BYTEORDER_LITTLEENDIAN
                            };
    SLDataSource slDataSource = {&androidBufferQueue,&pcm};
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX,outputMixObjext};
    SLDataSink androidSink = {&outputMix,NULL};
    const  SLInterfaceID  ids[1] = {SL_IID_BUFFERQUEUE};
    const  SLboolean  req[1] = {SL_BOOLEAN_TRUE};

    (*engineEngine)->CreateAudioPlayer(engineEngine,&pcmPlayer,&slDataSource,&androidSink,1,ids,req);

    (*pcmPlayer)->Realize(pcmPlayer,SL_BOOLEAN_FALSE);
    (*pcmPlayer)->GetInterface(pcmPlayer,SL_IID_PLAY,&pcmPlayerItf);

    //设置缓冲
    (*pcmPlayer)->GetInterface(pcmPlayer,SL_IID_BUFFERQUEUE,&pcmBufferQueue);
    (*pcmBufferQueue)->RegisterCallback(pcmBufferQueue,pcmBufferCallBack,NULL);

    (*pcmPlayerItf)->SetPlayState(pcmPlayerItf,SL_PLAYSTATE_PLAYING);
    pcmBufferCallBack(pcmBufferQueue,NULL);

    env->ReleaseStringUTFChars(path_, path);
}