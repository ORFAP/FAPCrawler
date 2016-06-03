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
 * A concrete consumer to print out data on the console.
 *
 * @param <T> the type of data to be printed
 */
public class Printer<T> extends BaseConsumer<T> {

	@Override
	public void accept(T data) {
		if (data != null) {
			System.out.println(data.toString());
		}
	}
}
