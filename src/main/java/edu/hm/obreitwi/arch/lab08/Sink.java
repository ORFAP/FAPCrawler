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
 * An active component which uses a consumer to request data from the pipeline.
 *
 * @param <T> the type of object the consumer processes
 */
public class Sink<T> extends Thread {

	/**
	 * The consumer to be used.
	 */
	private Consumer<T> consumer;

	/**
	 * Setter for the consumer. Also starts the thread.
	 *
	 * @param givenConsumer to be used
	 * @return the consumer
	 */
	public Consumer<T> use(Consumer<T> givenConsumer) {
		this.consumer = givenConsumer;
		start();
		return consumer;
	}

	@Override
	public void run() {
		if (consumer == null) {
			throw new IllegalStateException("consumer not yet set.");
		}
		T input;
		synchronized (this) {
			try {
				wait();     // Warten auf Interrupt
			} catch (InterruptedException expected) {
			}
		}
		do {
			input = consumer.getIncoming().pull();
			consumer.accept(input);
		} while (input != null);
	}
}
