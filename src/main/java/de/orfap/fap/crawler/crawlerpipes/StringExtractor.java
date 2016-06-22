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
    public U deliver() {
        String output = null;
        try {
            output = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (output != null) {
            try {
                if (!output.matches("\"[0-9]{1,1}.*")) {
                    output = br.readLine();
                }
                //noinspection unchecked
                return (U) output;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void accept(T datum) {
    }

    public void setBr(BufferedReader br) {
        this.br = br;
    }
}
