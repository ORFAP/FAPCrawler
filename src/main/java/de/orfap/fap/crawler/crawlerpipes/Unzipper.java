package de.orfap.fap.crawler.crawlerpipes;

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
    private File file;
    private ZipFile zipfile;
    private String s;
    private BufferedReader br;

    public Unzipper(File file, String s) {
        this.file = file;
        this.s = s;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Unzipper(ZipFile zipfile, String s) {
        this.zipfile = zipfile;
        this.s = s;
        Enumeration entries = zipfile.entries();
        ZipEntry zE = (ZipEntry) entries.nextElement();
        try {
            br = new BufferedReader(new InputStreamReader(zipfile.getInputStream(zE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public U transform(T data) {
        return null;
    }

    @Override
    public U deliver() {
        if ((zipfile instanceof ZipFile || file instanceof File) && s instanceof String) {
            try {
                String output;
                output = br.readLine();
                if(output.startsWith("\"")){
                    output=br.readLine();
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
