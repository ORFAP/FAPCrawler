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
 * Interface for Filters. Transforms objects from one type to another.
 *
 * @param <T> type of objects to be transformed
 * @param <U> type of the object after transformation
 */
public interface Filter<T, U> extends Consumer<T>, Producer<U> {

	/**
	 * Transforms an object of type T into object of type U. null object is
	 * always given through without transformation.
	 *
	 * @param data the object to be transformed. Can be null
	 * @return the transformed object. Can be null
	 */
	U transform(T data);

	@Override
	default U deliver() {
		final T datum = getIncoming().pull();
		return transform(datum);
	}

	@Override
	default void accept(T datum) {
		U output;
		if (datum == null) {
			output = null;
		} else {
			output = transform(datum);
		}
		getOutgoing().push(output);
	}
}
