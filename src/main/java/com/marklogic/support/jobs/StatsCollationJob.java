package com.marklogic.support.jobs;

import com.marklogic.support.util.Requests;
import com.marklogic.support.util.Util;
import com.marklogic.support.util.XQueryBuilder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class StatsCollationJob implements Job {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    String[] hosts = Util.getConfiguration().getStringArray("hosts");
    String[] databases = Util.getConfiguration().getStringArray("databases");

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //LOG.debug("DataLoaderJob: Loading more documents into MarkLogic Server...");
        //Util.loadSampleDataIntoMarkLogic(hosts,databases,databaseStringRangeIndexes);
        // TODO - this is hardcoded to get the first item right now!  Would need to be modified for multiple databases
        LOG.info(Util.processHttpRequestAndGetBody(Requests.evaluateXQuery(hosts[0], XQueryBuilder.evaluateXQueryModuleAgainstDatabase(databases[0], "src/main/resources/triple-stats.xqy") )));
        LOG.info(Util.processHttpRequestAndGetBody(Requests.evaluateXQuery(hosts[0], XQueryBuilder.evaluateXQueryModuleAgainstDatabase(databases[0], "src/main/resources/dump-unclosed-stands.xqy") )));

    }
}
