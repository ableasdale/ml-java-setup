package com.marklogic.support.jobs;

import com.marklogic.support.util.Requests;
import com.marklogic.support.util.Util;
import com.marklogic.support.util.XQueryBuilder;
import com.marklogic.support.util.XQueryDataBuilder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class DataLoaderJob implements Job {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    String[] hosts = Util.getConfiguration().getStringArray("hosts");
    String[] databases = Util.getConfiguration().getStringArray("databases");
    String[] databaseStringRangeIndexes = Util.getConfiguration().getStringArray("databasestringrangeindexes");

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.info("Loading more documents into MarkLogic Server...");

        for (String h : hosts) {
            for (String d : databases) {
                Util.processHttpRequest(Requests.evaluateXQuery(h, XQueryDataBuilder.createSampleDocData(d, databaseStringRangeIndexes)));
                //Util.processHttpRequest(Requests.evaluateXQuery(h, XQueryBuilder.createSampleDocData(d)));
            }
        }

    }
}
