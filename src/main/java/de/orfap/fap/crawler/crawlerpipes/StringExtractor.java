package de.orfap.fap.crawler.crawlerpipes;

import edu.hm.obreitwi.arch.lab08.BaseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by o4 on 03.06.16.
 */
public class StringExtractor<T, U> extends BaseFilter<T, U> {
    private final Logger LOG = LoggerFactory.getLogger(StringExtractor.class);
    private String downloadfileType;
    private String filename;
    private String s;
    private BufferedReader br;

    public StringExtractor(final String downloadfileType, final String filename, final String s) {
        this.downloadfileType = downloadfileType;
        this.filename = filename;
        this.s = s;
        if (downloadfileType.equals("csv")) {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (downloadfileType.equals("zip")) {
            try {
                ZipFile zipfile = new ZipFile(filename);
                Enumeration entries = zipfile.entries();
                ZipEntry zE = (ZipEntry) entries.nextElement();
                br = new BufferedReader(new InputStreamReader(zipfile.getInputStream(zE)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Filetype not supported");
        }
    }

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
}
