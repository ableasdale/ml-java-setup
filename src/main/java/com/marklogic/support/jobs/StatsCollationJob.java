package com.marklogic.support.jobs;

import com.marklogic.support.beans.BackupStats;
import com.marklogic.support.beans.StatsTracker;
import com.marklogic.support.providers.MarkLogicContentSourceProvider;
import com.marklogic.support.providers.Statistics;
import com.marklogic.support.util.Util;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StatsCollationJob implements Job {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    String[] hosts = Util.getConfiguration().getStringArray("hosts");
    String[] databases = Util.getConfiguration().getStringArray("databases");

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        // Note: this is hardcoded to get the first item right now - this is okay for my current testing but should be fixed so we store these maps for each database and handle reporting accordingly (TODO)
        try {
            Session s = MarkLogicContentSourceProvider.getInstance().getContentSource().newSession(databases[0]);

            Request r = s.newAdhocQuery(new String(Files.readAllBytes(Paths.get("src/main/resources/create-stats-tracker-xml.xqy"))));
            ResultSequence rs = s.submitRequest(r);
            StatsTracker st = Util.createStatsObjectFromXml(rs.asString());
            Statistics.getStatisticsMap().put(st.getDateTimeOnServer(), st);
            rs.close();

            r = s.newAdhocQuery(new String(Files.readAllBytes(Paths.get("src/main/resources/backup-status.xqy"))));
            rs = s.submitRequest(r);
            BackupStats b = Util.createBackupDataObjectFromXml(rs.asString());
            Statistics.getBackupStatisticsMap().put(b.getDateTimeOnServer(), b);
            rs.close();

            LOG.info("Stats Map Size: " + Statistics.getStatisticsMap().size() + " BackupStats Map Size: " + Statistics.getBackupStatisticsMap().size());
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }
}
