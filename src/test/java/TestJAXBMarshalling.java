import com.marklogic.support.beans.BackupStats;
import com.marklogic.support.beans.StatsTracker;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJAXBMarshalling {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    String xml = "<statsTracker><dateTimeOnServer>2018-02-16T02:00:17.512307-08:00</dateTimeOnServer><earliestDateTime>2018-02-16T01:55:47-08:00</earliestDateTime><ageOfEarliestDateTime>PT4M30.512307S</ageOfEarliestDateTime><earliestDateTimeRange>PT3M2S</earliestDateTimeRange><latestDateTimeRange>PT57S</latestDateTimeRange><idOfEarliestStand>17045620195599742996</idOfEarliestStand><pathOfEarliestStand>/space/data/Forests/PrimaryApplication-1/00000014</pathOfEarliestStand><totalDocs>18311</totalDocs><totalTriples>301100</totalTriples><totalUnclosedStands>10</totalUnclosedStands><totalUniqueObjects>301100</totalUniqueObjects><totalUniquePredicates>1493</totalUniquePredicates><totalUniqueSubjects>1501</totalUniqueSubjects><rawOutput>Id: 17045620195599742996 Path: /space/data/Forests/PrimaryApplication-1/00000014 Earliest: 2018-02-16T01:55:47-08:00 Latest: 2018-02-16T01:59:52-08:00 \n" +
            " Id: 11934782200106519284 Path: /space/data/Forests/PrimaryApplication-6/00000019 Earliest: 2018-02-16T01:57:00-08:00 Latest: 2018-02-16T01:59:00-08:00 \n" +
            " Id: 935942285127921848 Path: /space/data/Forests/PrimaryApplication-2/00000014 Earliest: 2018-02-16T01:57:01-08:00 Latest: 2018-02-16T01:59:00-08:00 \n" +
            " Id: 5856254323127327439 Path: /space/data/Forests/PrimaryApplication-1/00000015 Earliest: 2018-02-16T01:57:01-08:00 Latest: 2018-02-16T01:59:00-08:00 \n" +
            " Id: 2888847708052417776 Path: /space/data/Forests/PrimaryApplication-4/00000018 Earliest: 2018-02-16T01:57:01-08:00 Latest: 2018-02-16T01:59:00-08:00 \n" +
            " Id: 4025227218097015300 Path: /space/data/Forests/PrimaryApplication-3/00000015 Earliest: 2018-02-16T01:57:03-08:00 Latest: 2018-02-16T01:59:57-08:00 \n" +
            " Id: 10832301574181140709 Path: /space/data/Forests/PrimaryApplication-1/00000013 Earliest: 2018-02-16T01:57:03-08:00 Latest: 2018-02-16T01:59:52-08:00 \n" +
            " Id: 8888738260622679206 Path: /space/data/Forests/PrimaryApplication-3/00000018 Earliest: 2018-02-16T01:57:14-08:00 Latest: 2018-02-16T01:59:00-08:00 \n" +
            " Id: 15623427714656129849 Path: /space/data/Forests/PrimaryApplication-3/00000017 Earliest: 2018-02-16T01:58:05-08:00 Latest: 2018-02-16T01:59:57-08:00 \n" +
            " Id: 10433488193259735779 Path: /space/data/Forests/PrimaryApplication-5/00000019 Earliest: 2018-02-16T01:58:49-08:00 Latest: 2018-02-16T01:59:00-08:00 \n" +
            "</rawOutput></statsTracker>";

    String backupXml = "<backupStats><dateTimeOnServer>2018-02-17T03:33:37.82739-08:00</dateTimeOnServer><totalOverallBackupJobs>278</totalOverallBackupJobs><totalCompletedStatus>1046</totalCompletedStatus><totalNotCompletedStatus>1454</totalNotCompletedStatus><rawOutput>~~~</rawOutput></backupStats>";

    @Test
    void testXmlStructureFromBean() {
        StatsTracker s = new StatsTracker();

        s.setDateTimeOnServer("1");
        s.setAgeOfEarliestDateTime("x");
        s.setTotalDocs(1);
        s.setTotalTriples(1);

        s.setTotalUniqueSubjects(1);
        s.setTotalUniquePredicates(1);
        s.setTotalUniqueObjects(1);


        s.setTotalUnclosedStands(1);
        s.setEarliestDateTime("x");
        s.setEarliestDateTimeRange("x");
        s.setLatestDateTimeRange("y");

        s.setIdOfEarliestStand("1");


        s.setPathOfEarliestStand("/x");
        s.setRawOutput("RAW");

        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(StatsTracker.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(s, System.out);
           // jaxbMarshaller.ma
            //LOG.info(s);

        } catch (JAXBException e) {
            e.printStackTrace();
        }


    }


    @Test
    void testObjectFromXMLString() throws JAXBException {

    JAXBContext jaxbContext = JAXBContext.newInstance(StatsTracker.class);
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();


    StringReader reader = new StringReader(xml);
    StatsTracker s = (StatsTracker) unmarshaller.unmarshal(reader);

    assertEquals("2018-02-16T02:00:17.512307-08:00", s.getDateTimeOnServer());
    assertEquals("PT4M30.512307S", s.getAgeOfEarliestDateTime());
    //LOG.info("Datetime: "+ s.getDateTimeOnServer());

    }

    @Test
    void testXmlStructureFromBackupStats() {
        BackupStats b = new BackupStats();

        b.setDateTimeOnServer("2018-02-16T02:00:17.512307-08:00");
        b.setTotalCompletedStatus(12);
        b.setTotalNotCompletedStatus(9);
        b.setTotalOverallBackupJobs(5);
        b.setRawOutput("raw output");


        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(BackupStats.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(b, System.out);
            // jaxbMarshaller.ma
            //LOG.info(s);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    @Test
    void testBackupDataFromXMLString() throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(BackupStats.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        StringReader reader = new StringReader(backupXml);
        BackupStats b = (BackupStats) unmarshaller.unmarshal(reader);

        assertEquals("2018-02-17T03:33:37.82739-08:00", b.getDateTimeOnServer());
        assertEquals(1046, b.getTotalCompletedStatus());
        assertEquals(278, b.getTotalOverallBackupJobs());
        //LOG.info("Datetime: "+ s.getDateTimeOnServer());

    }


}
