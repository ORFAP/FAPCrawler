/*
 * HM, FK07
 * Vorlesung Software Engineering II, Praktikum
 * Otto Leo Breitwieser
 * 20160608
 * Ubuntu 16.04, Kernel 4.5.2
 * java 1.8.0_91
 * Intel(R) Core(TM) i5 CPU       M 540  @ 2.53GHz
 * MemTotal:        3840284 kB
 */
package edu.hm.obreitwi.arch.lab08;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * A pipe with a buffer of given size, to store objects when push/pull is
 * asynchronous.
 *
 * @param <T> type of object to be transferred
 */
public class SynchronizedQueue<T> extends Pipe<T> {

    /**
     * The buffer.
     */
    private final LinkedBlockingDeque<T> buffer;

    /**
     * Custom Konstruktor which creates a buffer of the given size.
     *
     * @throws IllegalArgumentException if size is less than 1
     */
    public SynchronizedQueue() {
        this.buffer = new LinkedBlockingDeque<>();
    }

    @Override
    public void push(T datum) {
        if (datum != null) {
            buffer.offer(datum);
        }
    }

    @Override
    public T pull() {
        T output = null;
        try {
            output = buffer.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }
}
