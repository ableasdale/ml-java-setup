package com.marklogic.support.jobs;

import com.marklogic.support.util.Requests;
import com.marklogic.support.util.Util;
import com.marklogic.support.util.XQueryBuilder;
import org.apache.commons.io.FileUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;

public class ClusterInformationGathererJob implements Job {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    String[] hosts = Util.getConfiguration().getStringArray("hosts");
    String[] databases = Util.getConfiguration().getStringArray("databases");

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        for(String d : databases) {
            LOG.info(String.format("Dumping forest status for: %s", d));
            try {
                FileUtils.writeStringToFile(new File(String.format("/tmp/%sforeststatus%d.xml", d, System.currentTimeMillis() / 1000L)), Util.processHttpRequestAndGetBody(Requests.evaluateXQuery(hosts[0], XQueryBuilder.getForestStatusForDatabase(d))), Charset.forName("UTF-8"));
            } catch (IOException e) {
                LOG.error("IOException Caught",e);
            }
        }
    }
}
