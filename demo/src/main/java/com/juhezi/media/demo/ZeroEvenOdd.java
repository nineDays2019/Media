package com.juhezi.media.demo;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.concurrent.Semaphore;
import java.util.function.IntConsumer;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ZeroEvenOdd {

    private int n;

    private Semaphore z, e, o;

    public ZeroEvenOdd(int n) {
        this.n = n;
        z = new Semaphore(1);
        e = new Semaphore(0);
        o = new Semaphore(0);
    }

    public void zero(IntConsumer printNumber) throws InterruptedException {
        for (int i = 0; i < n; i++) {
            z.acquire();
            printNumber.accept(0);
            if ((i & 1) == 0) {
                o.release();
            } else {
                e.release();
            }
        }
    }

    // 偶数
    public void even(IntConsumer printNumber) throws InterruptedException {
        for (int i = 2; i <= n; i += 2) {
            e.acquire();
            printNumber.accept(i);
            z.release();
        }
    }

    // 奇数
    public void odd(IntConsumer printNumber) throws InterruptedException {
        for (int i = 1; i <= n; i += 2) {
            o.acquire();
            printNumber.accept(i);
            z.release();
        }
    }
}