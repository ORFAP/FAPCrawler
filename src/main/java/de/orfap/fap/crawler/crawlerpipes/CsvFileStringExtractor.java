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

    @Override
    public String deliver() {
        String output = null;
        try {
            output = getBr().readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (output != null) {
            try {
                while (!output.matches("\"[0-9]{1,1}.*") || output.contains(",,")) {
                    getLOG().debug("Wrong formatted line in CsvFile: " + output);
                    output = getBr().readLine();
                    if(output==null){
                        return output;
                    }
                }
                //noinspection unchecked
                return output;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            getBr().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
