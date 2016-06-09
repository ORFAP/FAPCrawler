/*
 * HM, FK07
 * Vorlesung Softwarearchitektur I, Praktikum
 * Otto Leo Breitwieser, Fabian Uhlmann
 * 20150616
 * Ubuntu 15.04, Kernel 4.0.5
 * java 1.8.0_45
 * Intel(R) Core(TM) i5 CPU       M 540  @ 2.53GHz
 * MemTotal:        3840284 kB
 */
package edu.hm.obreitwi.arch.lab08;

/**
 * ABC for consumers.
 *
 * @param <T> type of objects to be processed
 */
public abstract class BaseConsumer<T> implements Consumer<T> {

	/**
	 * The pipe this consumer is fed from.
	 */
	private Pipe<T> pipe;

	@Override
	public void connectIncoming(Pipe<T> givenPipe) {
		this.pipe = givenPipe;
	}

	@Override
	public Pipe<T> getIncoming() {
		return pipe;
	}
}
