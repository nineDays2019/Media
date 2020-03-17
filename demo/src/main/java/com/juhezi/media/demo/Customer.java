package com.juhezi.media.demo;

import android.util.Log;

public class Customer {

    private static final String TAG = "Customer";

    private Object lock;

    public Customer(Object lock) {
        this.lock = lock;
    }

    public void getValue() {
        try {
            synchronized (lock) {
                if (ValueObject.value.isEmpty()) {
                    lock.wait();
                }
                System.out.println("#getValue: " + ValueObject.value);
                ValueObject.value = "";
                lock.notify();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
