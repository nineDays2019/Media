package com.juhezi.media.demo;

import java.util.concurrent.atomic.AtomicInteger;

public class FooBar {

    private int n;
    private final AtomicInteger tag = new AtomicInteger();
    private final Object lock = new Object();

    public FooBar(int n) {
        this.n = n;
        tag.set(0);
    }

    public void foo(Runnable printFoo) throws InterruptedException {
        for (int i = 0; i < n; i++) {
            synchronized (lock) {
                while (tag.get() != i * 2) {
                    lock.wait();
                }
                printFoo.run();
                tag.incrementAndGet();
                lock.notifyAll();
            }
        }
    }

    public void bar(Runnable printBar) throws InterruptedException {
        for (int i = 0; i < n; i++) {
            synchronized (lock) {
                while (tag.get() != 2 * i + 1) {
                    lock.wait();
                }
                printBar.run();
                tag.incrementAndGet();
                lock.notifyAll();
            }
        }
    }
}