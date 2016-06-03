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
 * An active component for the pipeline which drives a producer to feed the
 * pipeline.
 *
 * @param <T> the type of objects the producer creates
 */
public class Pump<T> extends Thread {

	/**
	 * The producer to be driven.
	 */
	private Producer<T> producer;

	/**
	 * Setter for the producer. Also starts the thread.
	 *
	 * @param givenProducer to be used
	 * @return the producer
	 */
	public Producer<T> use(Producer<T> givenProducer) {
		this.producer = givenProducer;
		start();
		return producer;
	}

	@Override
	public void run() {
		if (producer == null) {
			throw new IllegalStateException("producer not yet set.");
		}
		T output;
		synchronized (this) {
			try {
				wait();     // Warten auf Interrupt
			} catch (InterruptedException expected) {
			}
		}
		do {
			output = producer.deliver();
			producer.getOutgoing().push(output);
		} while (output != null);
	}
}
