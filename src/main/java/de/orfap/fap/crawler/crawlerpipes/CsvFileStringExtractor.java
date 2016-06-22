package de.orfap.fap.crawler.crawlerpipes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by o4 on 22.06.2016.
 */
public class CsvFileStringExtractor extends StringExtractor<File, String> {

    public CsvFileStringExtractor(final String filename) {
        try {
            setBr(new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
