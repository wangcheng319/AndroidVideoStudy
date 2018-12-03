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



extern "C"
JNIEXPORT void JNICALL
Java_com_wangc_myplayer_Demo_PcmToMp3(JNIEnv *env, jobject instance) {

    mp3Test = new PcmToMp3Test();
    mp3Test->enCode();

}