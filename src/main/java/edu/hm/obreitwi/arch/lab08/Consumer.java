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
 * Interface for Consumers.
 *
 * @param <T> the type of objects to be processed
 */
public interface Consumer<T> {

	/**
	 * Method to process given data.
	 *
	 * @param data to be processed
	 */
	void accept(T data);

	/**
	 * Connects a given pipe, then returns it.
	 *
	 * @param pipe to be connected
	 */
	void connectIncoming(Pipe<T> pipe);

	/**
	 * Getter for the incoming pipe.
	 *
	 * @return the incoming pipe
	 */
	Pipe<T> getIncoming();
}
