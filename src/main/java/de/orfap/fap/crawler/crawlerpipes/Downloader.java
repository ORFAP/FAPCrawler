package de.orfap.fap.crawler.crawlerpipes;

import edu.hm.obreitwi.arch.lab08.BaseProducer;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by o4 on 03.06.16.
 */
public class Downloader<T> extends BaseProducer<T> {
    Object object;
    private String url;

    public Downloader(String url, Object object) {
        this.url = url;
        this.object = object;
    }

    @Override
    public T deliver() {
        T output = null;
        String filename;
        try {
            URL urlToRead = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlToRead.openConnection();
            conn.setRequestMethod("GET");
            filename = conn.getHeaderField("Content-Disposition").split("filename=")[1].replace("\"", "");
            Files.copy(conn.getInputStream(), Paths.get(filename));
            if (object instanceof File) {
                //noinspection unchecked
                return (T) new File(filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }
}
