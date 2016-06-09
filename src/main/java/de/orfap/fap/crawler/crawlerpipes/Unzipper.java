package de.orfap.fap.crawler.crawlerpipes;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import edu.hm.obreitwi.arch.lab08.BaseFilter;

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
public class Unzipper<T, U> extends BaseFilter<T, U> {
    private String downloadfileType;
    private String filename;
    private String s;
    private BufferedReader br;

    public Unzipper(final String downloadfileType, final String filename, final String s) {
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
        }
    }

    @Override
    public U transform(T data) {
        return null;
    }

    @Override
    public U deliver() {
        if ((downloadfileType.equals("csv") || downloadfileType.equals("zip")) && s instanceof String) {
            try {
                String output;
                output = br.readLine();
                if (output.startsWith("\"")) {
                    output = br.readLine();
                }
                //noinspection unchecked
                return (U) output;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void accept(T datum) {

    }
}
