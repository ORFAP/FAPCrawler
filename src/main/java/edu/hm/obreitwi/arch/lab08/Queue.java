/*
 * HM, FK07
 * Vorlesung Softwarearchitektur I, Praktikum
 * Otto Leo Breitwieser, Fabian Uhlmann
 * 20150618
 * Ubuntu 15.04, Kernel 4.0.5
 * java 1.8.0_45
 * Intel(R) Core(TM) i5 CPU       M 540  @ 2.53GHz
 * MemTotal:        3840284 kB
 */
package edu.hm.obreitwi.arch.lab08;

import java.util.LinkedList;
import java.util.List;

/**
 * A pipe with a buffer of given size, to store objects when push/pull is
 * asynchronous.
 *
 * @param <T> type of object to be transferred
 */
public class Queue<T> extends Pipe<T> {

	/**
	 * The buffer.
	 */
	private final List<T> buffer;
	/**
	 * The size of the buffer.
	 */
	private final int size;

	/**
	 * Custom Konstruktor which creates a buffer of the given size.
	 *
	 * @param size of the buffer
	 * @throws IllegalArgumentException if size is less than 1
	 */
	public Queue(int size) {
		if (size < 1) {
			throw new IllegalArgumentException();
		}
		this.size = size;
		this.buffer = new LinkedList<>();
	}

	@Override
	public synchronized void push(T datum) {
		while (buffer.size() >= size) {
			try {
				wait();
			} catch (InterruptedException inex) {
				throw new AssertionError("There is no spoon.");
			}
		}
		buffer.add(datum);
		notifyAll();
	}

	@Override
	public synchronized T pull() {
		while (buffer.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException inex) {
				throw new AssertionError("I am a Mac.");
			}
		}
		final T output = buffer.remove(0);
		notifyAll();
		return output;
	}
}
