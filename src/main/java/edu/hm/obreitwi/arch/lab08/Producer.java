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
 * Interface for producers.
 *
 * @param <T> the type of objects to be created
 */
public interface Producer<T> {

	/**
	 * Gives out an object of type T or null.
	 *
	 * @return the object. Can be null
	 */
	T deliver();

	/**
	 * Connects a given pipe, then returns it.
	 *
	 * @param pipe to be connected
	 * @return the pipe
	 */
	Pipe<T> connect(Pipe<T> pipe);

	/**
	 * Getter for the outgoing pipe.
	 *
	 * @return the outgoing pipe
	 */
	Pipe<T> getOutgoing();
}
