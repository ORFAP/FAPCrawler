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
 * An active component which uses a filter to get objects from the pipeline,
 * transform them, and then send them onwards.
 *
 * @param <T> the type of object to be transformed
 * @param <U> the type of the transformed object
 */
public class Worker<T, U> extends Thread {

	/**
	 * The filter to be used.
	 */
	private Filter<T, U> filter;

	/**
	 * Setter for the filter. Also starts the thread.
	 *
	 * @param givenFilter to be used
	 * @return the filter
	 */
	public Filter<T, U> use(Filter<T, U> givenFilter) {
		this.filter = givenFilter;
		start();
		return filter;
	}

	@Override
	public void run() {
		if (filter == null) {
			throw new IllegalStateException("filter not yet set.");
		}
		U output;
		synchronized (this) {
			try {
				wait();     // Warten auf Interrupt
			} catch (InterruptedException expected) {
			}
		}
		do {
			output = filter.deliver();
			filter.getOutgoing().push(output);
		} while (output != null);
	}

}
