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
 * ABC for producers.
 *
 * @param <T> the type of objects to be created
 */
public abstract class BaseProducer<T> implements Producer<T> {

	/**
	 * The pipe where the created objects go to.
	 */
	private Pipe<T> pipe;

	@Override
	public Pipe<T> connect(Pipe<T> givenPipe) {
		this.pipe = givenPipe;
		this.pipe.connectIncoming(this);
		return this.pipe;
	}

	@Override
	public Pipe<T> getOutgoing() {
		return pipe;
	}
}
