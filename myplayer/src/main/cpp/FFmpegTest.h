//
// Created by wangc on 2018/12/5.
//

/**
 * 熟悉FFmpeg各种编解码流程，格式转换
 */

#ifndef ANDROIDVIDEOSTUDY_FFMPEGTEST_H
#define ANDROIDVIDEOSTUDY_FFMPEGTEST_H
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wangc",FORMAT,##__VA_ARGS__);

class FFmpegTest {



public:
    void mEnCode();

    void createThread();
};


#endif //ANDROIDVIDEOSTUDY_FFMPEGTEST_H
