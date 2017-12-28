package com.marklogic.support;

import com.marklogic.support.actions.BaseSystemConfiguration;
import com.marklogic.support.beans.SSHClientConnection;
import com.marklogic.support.util.MarkLogicConfig;
import com.marklogic.support.util.Requests;
import com.marklogic.support.util.Util;
import com.marklogic.support.util.XQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiNodeClusterSetup {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static ExecutorService es = Executors.newFixedThreadPool(64);

    public static void main(String[] args) {

        Util.jcePolicyFix();
        String[] hosts = Util.getConfiguration().getStringArray("hosts");
        String[] databases = Util.getConfiguration().getStringArray("databases");
        int forestsperhost = Util.getConfiguration().getInt("forestsperhost");
        String dataDirectory = Util.getConfiguration().getString("datadirectory");
        List<SSHClientConnection> clientConnectionList = new ArrayList<>();

        // Part One: Base Configuration of all hosts over ssh
        for (String s : hosts) {
            SSHClientConnection sshcc = new SSHClientConnection();
            sshcc.setName(s);
            sshcc.setClient(Util.initializeHost(s));
            clientConnectionList.add(sshcc);
            es.submit(new BaseSystemConfiguration(sshcc));
        }

        // Stop the thread pool
        es.shutdown();
        // Drain the queue
        while (!es.isTerminated()) {
            try {
                es.awaitTermination(72, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                LOG.error("Exception caught: ", e);
            }
        }

        // Part Two - Initialize the primary node using the MarkLogic ReST API
        Util.processHttpRequest(Requests.configurePrimaryNode(hosts[0]));
        LOG.info(String.format("Master host (%s) should now be configured with the default databases", hosts[0]));

        /* Placeholder until I write code to do the timestamp polling request... */
        try {
            // TODO - this may well be way too short!
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            LOG.error("Exception caught: ", e);
        }

        // TODO - Set group level logging to debug
        // admin:group-set-file-log-level($config, $groupid, "debug")
        Util.processHttpRequest(Requests.evaluateXQuery(hosts[0], XQueryBuilder.configureBaseGroupSettings()));

        // Part Three - Join all additional nodes to the master host
        for (int i=1; i < hosts.length; i++) {
            MarkLogicConfig.addHostToCluster(hosts[i], hosts[0]);
        }

        // Part Four - configure Databases and Forests
        Util.processHttpRequest(Requests.evaluateXQuery(hosts[0], XQueryBuilder.createDatabaseAndForests(hosts, databases, dataDirectory, forestsperhost)));

        /* curl --anyauth --user admin:admin -i -X POST -d'{"rest-api":{"name":"PrimaryApplication"}}' -H "Content-type: application/json" http://localhost:8002/LATEST/rest-apis */
        /*for (String db : databases){
            //Util.processHttpRequest(Requests.createDatabase(hosts[0], db));
        }*/

        // Part Five - Create test data
        // TODO - move this into a scheduled task
        for (String h : hosts) {
            for (String d : databases) {
               // Util.processHttpRequest(Requests.evaluateXQuery(h, XQueryBuilder.createSampleDocData(d)));
            }
        }

        LOG.info(String.format("Configuration should now be complete; log into http://%s:8001 to inspect the cluster configuration.", hosts[0]));

    }
}
