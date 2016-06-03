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
 * a concrete producer which gives out Integers starting from 0 up to a given
 * limit.
 */
public class IntsUpto extends BaseProducer<Integer> {

	/**
	 * The highest value to be given out.
	 */
	private final int limit;
	/**
	 * The last value that was given out.
	 */
	private int now;

	/**
	 * Custom Konstruktor.
	 *
	 * @param limit to be used
	 */
	public IntsUpto(int limit) {
		this.limit = limit;
		now = 0;
	}

	@Override
	public Integer deliver() {
		Integer output;
		if (now < limit) {
			output = now;
			now++;
		} else {
			output = null;
		}
		return output;
	}
}
