package com.juhezi.media.demo;

import java.util.concurrent.atomic.AtomicInteger;

// 1 2 3 按顺序执行
public class Foo {

    private final AtomicInteger i = new AtomicInteger();
    private final Object lock = new Object();

    public Foo() {
        i.set(0);
    }

    public void first(Runnable printFirst) throws InterruptedException {
        synchronized (lock) {
            while (i.get() != 0) {
                lock.wait();
            }
            printFirst.run();
            i.set(1);
            lock.notifyAll();
        }
    }

    public void second(Runnable printSecond) throws InterruptedException {
        synchronized (lock) {
            while (i.get() != 1) {
                lock.wait();
            }
            printSecond.run();
            i.set(2);
            lock.notifyAll();
        }
    }

    public void third(Runnable printThird) throws InterruptedException {
        synchronized (lock) {
            while (i.get() != 2) {
                lock.wait();
            }
            printThird.run();
            i.set(3);
        }
    }

}
