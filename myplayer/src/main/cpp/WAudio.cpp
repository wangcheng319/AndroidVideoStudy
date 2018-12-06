//
// Created by wangc on 2018/11/16.
//

#include "WAudio.h"

WAudio::WAudio(Wstatus *wstatus) {
    this->wstatus = wstatus;
    wqueue = new Wqueue(wstatus);

}

WAudio::~WAudio() {

}
