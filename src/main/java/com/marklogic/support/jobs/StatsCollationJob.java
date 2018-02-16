package com.marklogic.support.jobs;

import com.marklogic.support.util.Requests;
import com.marklogic.support.providers.Statistics;
import com.marklogic.support.util.Util;
import com.marklogic.support.util.XQueryBuilder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.UUID;

public class StatsCollationJob implements Job {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    String[] hosts = Util.getConfiguration().getStringArray("hosts");
    String[] databases = Util.getConfiguration().getStringArray("databases");

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // TODO - get XCC to return the necessary data and turn into the necessary Java Object
        Statistics.getStatisticsMap().put("x"+UUID.randomUUID(), null);
        LOG.info("Map Size: " + Statistics.getStatisticsMap().size());

        // Note: this is hardcoded to get the first item right now - this is okay for my current testing but should be fixed so we store these maps for each database and handle reporting accordingly (TODO)
        LOG.info(Util.processHttpRequestAndGetBody(Requests.evaluateXQuery(hosts[0], XQueryBuilder.evaluateXQueryModuleAgainstDatabase(databases[0], "src/main/resources/create-stats-tracker-xml.xqy") )));
    }
}
