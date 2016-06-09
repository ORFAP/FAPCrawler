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

import java.util.function.Function;

/**
 * A concrete filter which transforms given objects by a given function.
 *
 * @param <T> type of object to be transformed
 * @param <U> type of transformed object
 */
public class FunctionFilter<T, U> extends BaseFilter<T, U> {

	/**
	 * The function to be used.
	 */
	private final Function<T, U> function;

	/**
	 * Custom Konstruktor.
	 *
	 * @param function to be used
	 */
	public FunctionFilter(Function<T, U> function) {
		this.function = function;
	}

	@Override
	public U transform(T data) {
		U output;
		if (data == null) {
			output = null;
		} else {
			output = function.apply(data);
		}
		return output;
	}

	@Override
	public void accept(T data) {
		U output;
		if (data == null) {
			output = null;
		} else {
			output = transform(data);
		}
		getOutgoing().push(output);
	}

	@Override
	public U deliver() {
		final T datum = getIncoming().pull();
		U output;
		if (datum == null) {
			output = null;
		} else {
			output = transform(datum);
		}
		return output;
	}
}
