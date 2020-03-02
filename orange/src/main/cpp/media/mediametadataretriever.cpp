//
// Created by juhezi on 20-3-1.
//

#include "mediametadataretriever.hpp"

MediaMetadataRetriever::MediaMetadataRetriever() {
    state = NULL;
}

MediaMetadataRetriever::~MediaMetadataRetriever() {
    Mutex::Autolock _l(mLock);
    ::release(&state);
}

int MediaMetadataRetriever::setDataSource(const char *dataSourceUrl, const char *headers) {
    Mutex::Autolock _l(mLock);
    return ::set_data_source_uri(&state, dataSourceUrl, headers);
}

int MediaMetadataRetriever::setDataSource(int fd, int64_t offset, int64_t length) {
    Mutex::Autolock _l(mLock);
    return ::set_data_source_fd(&state, fd, offset, length);
}