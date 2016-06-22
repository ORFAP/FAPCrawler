package de.orfap.fap.crawler.crawlerpipes;

import edu.hm.obreitwi.arch.lab08.BaseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by o4 on 03.06.16.
 */
public class StringExtractor<T, U> extends BaseFilter<T, U> {
    private final Logger LOG = LoggerFactory.getLogger(StringExtractor.class);
    private BufferedReader br;

    @Override
    public U transform(T data) {
        return null;
    }

    @Override
    public void accept(T datum) {
    }

    public void setBr(BufferedReader br) {
        this.br = br;
    }

    public BufferedReader getBr() {
        return br;
    }

    public Logger getLOG() {
        return LOG;
    }
}
