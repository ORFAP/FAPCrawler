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
 * A pipe which connects a producer with a consumer.
 *
 * @param <T> the type of objects to be transferred
 */
public class Pipe<T> {

	/**
	 * The producer where the objects come from.
	 */
	private Producer<T> producer;
	/**
	 * The consumer where the objects go to.
	 */
	private Consumer<T> consumer;

	/**
	 * Method to get an object from the producer.
	 *
	 * @return the object or null
	 */
	public T pull() {
		if (producer == null) {
			throw new IllegalStateException("producer not yet set.");
		}
		T output;
		output = producer.deliver();
		return output == null ? null : output;
	}

	/**
	 * Method to transfer a given object to the consumer.
	 *
	 * @param datum the object. Can be null
	 */
	public void push(T datum) {
		if (consumer == null) {
			throw new IllegalStateException("consumer not yet set.");
		}
		T output;
		output = datum == null ? null : datum;
		consumer.accept(output);
	}

	/**
	 * Method to connect a given consumer to the pipe.
	 *
	 * @param givenConsumer to be connected
	 * @return the consumer
	 */
	public Consumer<T> connect(Consumer<T> givenConsumer) {
		this.consumer = givenConsumer;
		consumer.connectIncoming(this);
		return consumer;
	}

	/**
	 * Method to connect a filter (as a consumer) to the pipe.
	 *
	 * @param <U> not needed in this context
	 * @param givenFilter to be connected
	 * @return the filter
	 */
	public <U> Filter<T, U> connect(Filter<T, U> givenFilter) {
		this.consumer = givenFilter;
		consumer.connectIncoming(this);
		return givenFilter;
	}

	/**
	 * Method to connect a producer to this pipe.
	 *
	 * @param givenProducer to be connected
	 */
	public void connectIncoming(Producer<T> givenProducer) {
		this.producer = givenProducer;
	}
}
