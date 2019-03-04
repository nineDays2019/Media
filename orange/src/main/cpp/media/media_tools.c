//
// Created by yunrui on 2019/3/4.
//
#include "media_tools.h"

struct URLProtocol;

char *getUrlProtocolInfo() {
    char info[40000] = {0};
    av_register_all();

    struct URLProtocol *pup = NULL;
    // Input
    struct URLProtocol **p_temp = &pup;
    avio_enum_protocols((void **) p_temp, 0);
    while ((*p_temp) != NULL) {
        sprintf(info, "%s[In ][%10s]\n", info, avio_enum_protocols((void **) p_temp, 0));
    }
    pup = NULL;
    // Output
    avio_enum_protocols((void **) p_temp, 1);
    while ((*p_temp) != NULL) {
        sprintf(info, "%s[Out][%10s]\n", info, avio_enum_protocols((void **) p_temp, 1));
    }
    return info;
}