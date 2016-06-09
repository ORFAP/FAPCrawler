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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

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
	private final SynchronousQueue<T> buffer;

	/**
	 * Custom Konstruktor which creates a buffer of the given size.
	 *
	 * @throws IllegalArgumentException if size is less than 1
	 */
	public SynchronizedQueue() {
		this.buffer = new SynchronousQueue<>();
	}

	@Override
	public void push(T datum) {
		buffer.offer(datum);
	}

	@Override
	public T pull() {
		return buffer.poll();
	}
}
