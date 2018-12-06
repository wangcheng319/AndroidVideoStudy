//
// Created by wangc on 2018/12/6.
//

#include "Wqueue.h"
#include "AndroidLog.h"

Wqueue::Wqueue(Wstatus *playStatus) {
    this->playStatus = playStatus;
    pthread_mutex_init(&mutexPacket,NULL);
    pthread_cond_init(&condPacket,NULL);
}


Wqueue::~Wqueue() {
    pthread_mutex_destroy(&mutexPacket);
    pthread_cond_destroy(&condPacket);

}

int Wqueue::putAvPacket(AVPacket *avPacket) {
    pthread_mutex_lock(&mutexPacket);

    queuePacket.push(avPacket);
    LOGE("放入一个AVpacket到队列，size= %d",queuePacket.size());
    pthread_cond_signal(&condPacket);

    pthread_mutex_unlock(&mutexPacket);

    return 0;
}

int Wqueue::getAvPacket(AVPacket *packet) {

    pthread_mutex_lock(&mutexPacket);

    while (playStatus != NULL && !playStatus->exit){
        if (queuePacket.size()>0){
            //取最前面的
            AVPacket *avPacket = queuePacket.front();
            //将avPacket的引用拷贝到packet中
            if (av_packet_ref(packet,avPacket)==0){
                queuePacket.pop();
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            LOGE("取出一个AVpacket，size= %d",queuePacket.size());
            break;
        } else{
            //没有数据就等待
            pthread_cond_wait(&condPacket,&mutexPacket);
        }
    }

    pthread_mutex_unlock(&mutexPacket);

    return 0;
}

int Wqueue::getQueueSize() {
    int size = 0;
    pthread_mutex_lock(&mutexPacket);
    size = queuePacket.size();
    pthread_mutex_unlock(&mutexPacket);
    return size;
}
