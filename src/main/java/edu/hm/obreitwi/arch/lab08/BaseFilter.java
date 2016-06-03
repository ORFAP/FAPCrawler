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
 * ABC for filters to transform objects from type T to type U.
 *
 * @param <T> type of object to be transformed
 * @param <U> type of transformed object
 */
public abstract class BaseFilter<T, U> implements Filter<T, U> {

	/**
	 * The pipe where the objects to be transformed come from.
	 */
	private Pipe<T> incoming;
	/**
	 * The pipe where the transformed objects go to.
	 */
	private Pipe<U> outgoing;

	@Override
	public void connectIncoming(Pipe<T> pipe) {
		this.incoming = pipe;
	}

	@Override
	public Pipe<T> getIncoming() {
		return incoming;
	}

	@Override
	public Pipe<U> getOutgoing() {
		return outgoing;
	}

	@Override
	public Pipe<U> connect(Pipe<U> pipe) {
		outgoing = pipe;
		pipe.connectIncoming(this);
		return pipe;
	}
}
