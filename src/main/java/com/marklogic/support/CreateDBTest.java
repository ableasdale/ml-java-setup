package com.marklogic.support;

import com.marklogic.support.util.Requests;
import com.marklogic.support.util.Util;
import com.marklogic.support.util.XQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class CreateDBTest {
    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        String[] hosts = Util.getConfiguration().getStringArray("hosts");
        String[] databases = Util.getConfiguration().getStringArray("databases");
        int forestsperhost = Util.getConfiguration().getInt("forestsperhost");
        String dataDirectory = Util.getConfiguration().getString("datadirectory");
        String[] databaseIndexSettings = Util.getConfiguration().getStringArray("databaseindexes");
        String[] databaseStringRangeIndexes = Util.getConfiguration().getStringArray("databasestringrangeindexes");


        /*
        for (String db : databases){
            String s = Util.processHttpRequestAndGetBody(Requests.createDatabase(hosts[0], db));
            LOG.info(s);
        } */
        //XQueryBuilder.createDatabaseAndForests(hosts, databases, dataDirectory, forestsperhost);
       // XQueryBuilder.createSampleDocData();
        LOG.info(XQueryBuilder.configureDatabaseStringRangeIndexes(databases, databaseStringRangeIndexes));
    }
}
