//
// Created by juhezi on 20-3-1.
//

#ifndef MEDIA_MUTEX_H
#define MEDIA_MUTEX_H

#include <stdint.h>
#include <sys/types.h>
#include <time.h>
#include <pthread.h>


// todo 这里的语法需要学习一下

class Condition;

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

    int32_t lock();

    void unlock();

    int32_t tryLock();

    // Manages the mutex automatically. It'll be locked when Autolock is
    // constructed and released when Autolock goes out of scope.
    class Autolock {
    public:
        inline Autolock(Mutex &mutex) : mLock(mutex) { mLock.lock(); }

        inline Autolock(Mutex *mutex) : mLock(*mutex) { mLock.lock(); }

        inline ~Autolock() { mLock.unlock(); }

    private:
        Mutex &mLock;
    };

private:
    friend class Condition;

    // A mutex cannot be copied
    Mutex(const Mutex &);

    Mutex &operator=(const Mutex &);

    pthread_mutex_t mMutex;
};

// ---------------------------------------------------------------------------

//#if defined(HAVE_PTHREADS)

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

inline int32_t Mutex::lock() {
    return -pthread_mutex_lock(&mMutex);
}

inline void Mutex::unlock() {
    pthread_mutex_unlock(&mMutex);
}

inline int32_t Mutex::tryLock() {
    return -pthread_mutex_trylock(&mMutex);
}

typedef Mutex::Autolock AutoMutex;

#endif //MEDIA_MUTEX_H
