//
// Created by juhezi on 19-3-13.
// PS: 复制自 Android 源码中的锁对象
//

#ifndef MEDIA_MUTEX_H
#define MEDIA_MUTEX_H

#include <stdint.h>
#include <sys/types.h>
#include <time.h>

#include <pthread.h>
#include "Errors.h"

class Mutex {
public:
    enum {
        PRIVATE = 0,
        SHARED = 1
    };

    Mutex();

    Mutex(const char *name);

    Mutex(int type, const char *name = NULL);

    ~Mutex();

    // lock or unlock the mutex
    status_t lock();

    status_t unlock();

    status_t tryLock();

    class Autolock {
    public:
        inline Autolock(Mutex &mutex) : mLock(mutex) {
            mLock.lock();
        }

        inline Autolock(Mutex *mutex) : mLock(*mutex) {
            mLock.lock();
        }

        inline ~Autolock() {
            mLock.unlock();
        }

    private:
        Mutex &mLock;
    };

private:
    friend class Condition;

    Mutex(const Mutex &);

    Mutex &operator=(const Mutex &);

    pthread_mutex_t mMutex;

};

inline Mutex::Mutex() {
    pthread_mutex_init(&mMutex, NULL);
}

inline Mutex::Mutex(const char *name) {
    pthread_mutex_init(&mMutex, NULL);
}

inline Mutex::Mutex(int type, const char *name) {
    if (type == SHARED) {
        pthread_mutexattr_t attr;
        pthread_mutexattr_init(&attr);
        pthread_mutexattr_setpshared(&attr, PTHREAD_PROCESS_SHARED);
        pthread_mutex_init(&mMutex, &attr);
        pthread_mutexattr_destroy(&attr);
    } else {
        pthread_mutex_init(&mMutex, NULL);
    }
}

inline Mutex::~Mutex() {
    pthread_mutex_destroy(&mMutex);
}

inline status_t Mutex::lock() {
    return ~pthread_mutex_lock(&mMutex);
}

inline status_t Mutex::unlock() {
    return ~pthread_mutex_unlock(&mMutex);
}

inline status_t Mutex::tryLock() {
    return pthread_mutex_trylock(&mMutex);
}

typedef Mutex::Autolock AutoMutex;

#endif //MEDIA_MUTEX_H
