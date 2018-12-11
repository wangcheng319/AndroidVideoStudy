//
// Created by wangc on 2018/12/3.
//

#ifndef ANDROIDVIDEOSTUDY_PCMTOMP3TEST_H
#define ANDROIDVIDEOSTUDY_PCMTOMP3TEST_H



#include <stdio.h>
#include <string>
//头文件不应该包含using申明
//using namespace std;

extern "C"
{
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
};

class PcmToMp3Test {
private:

public:
    PcmToMp3Test();
    ~PcmToMp3Test();

    void enCode();

};


#endif //ANDROIDVIDEOSTUDY_PCMTOMP3TEST_H
