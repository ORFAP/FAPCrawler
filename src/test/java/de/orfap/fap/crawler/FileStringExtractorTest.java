package de.orfap.fap.crawler;

import de.orfap.fap.crawler.crawlerpipes.CsvFileStringExtractor;
import de.orfap.fap.crawler.crawlerpipes.ZipFileStringExtractor;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Arne on 27.06.2016.
 */
public class FileStringExtractorTest {

    public static final int AIRLINENUMBERS = 1611;
    public static final int ROUTENUMBERS = 1231;

    @Test
    public void CsvTest(){
        CsvFileStringExtractor extractor = new CsvFileStringExtractor("src/test/resources/AirlineTest.csv");
        String nextLine = extractor.deliver();
        int count = 0;
        while(nextLine != null){
            count++;
            nextLine = extractor.deliver();
        }
        Assert.assertEquals(count, AIRLINENUMBERS);
    }

    @Test
    public void ZipTest(){
        ZipFileStringExtractor extractor = new ZipFileStringExtractor("src/test/resources/RoutesTest.zip");
        String nextLine = extractor.deliver();
        int count = 0;
        while(nextLine != null){
            count++;
            Assert.assertTrue(!nextLine.replace(",","").matches("(0-9)+"));
            nextLine = extractor.deliver();
        }
        Assert.assertEquals(ROUTENUMBERS,count);
    }
}
