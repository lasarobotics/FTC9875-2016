package org.firstinspires.ftc.teamcode.javascript;

/**
 * Created by Russell on 12/31/2016.
 */

public class ThreadSafeData<E> {
    private final Object lock = new Object();
    private volatile E value;

    public void setValue(E value) {
        synchronized(lock) {
            this.value = value;
        }
    }

    public E getValue() {
        synchronized(lock) {
            return value;
        }
    }
}