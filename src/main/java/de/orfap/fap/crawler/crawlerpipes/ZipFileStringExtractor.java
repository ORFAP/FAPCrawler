package de.orfap.fap.crawler.crawlerpipes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by o4 on 22.06.2016.
 */
public class ZipFileStringExtractor extends StringExtractor<ZipFile, String> {

    public ZipFileStringExtractor(String filename) {
        try {
            ZipFile zipfile = new ZipFile(filename);
            Enumeration entries = zipfile.entries();
            ZipEntry zE = (ZipEntry) entries.nextElement();
            setBr(new BufferedReader(new InputStreamReader(zipfile.getInputStream(zE))));
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
                while (!output.matches("[0-9].*") || output.contains(",,")) {
                    getLOG().debug("Wrong formatted line in ZipFile: " + output);
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
