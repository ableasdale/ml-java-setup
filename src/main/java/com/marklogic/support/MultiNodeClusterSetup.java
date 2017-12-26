package com.marklogic.support;

import com.marklogic.support.beans.SSHClientConnection;
import com.marklogic.support.util.MarkLogicConfig;
import com.marklogic.support.util.Requests;
import com.marklogic.support.util.Util;
import okhttp3.Response;
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
        List<SSHClientConnection> clientConnectionList = new ArrayList<>();

        // Part One
        for (String s : hosts) {
            // TODO - can this be done in concurrent threads to speed things up.
            SSHClientConnection sshcc = new SSHClientConnection();
            sshcc.setName(s);
            sshcc.setClient(Util.initializeHost(s));
            clientConnectionList.add(sshcc);
            es.submit(new BaseConfigurator(sshcc));
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

        // Part Two - initialize the primary node using the MarkLogic ReST API
        Util.processHttpRequest(Requests.configurePrimaryNode(hosts[0]));
        LOG.info(String.format("Master host (%s) should now be configured with the default databases", hosts[0]));

        /* Placeholder until I write code to do the timestamp polling request... */
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Part Three - join all additional nodes to the master host
        for (int i=1; i < hosts.length; i++) {
            MarkLogicConfig.addHostToCluster(hosts[i], hosts[0]);
        }

        // Part Four - configure Databases and Forests
        /* curl --anyauth --user admin:admin -i -X POST -d'{"rest-api":{"name":"PrimaryApplication"}}' -H "Content-type: application/json" http://localhost:8002/LATEST/rest-apis */
        for (String db : databases){
            Util.processHttpRequest(Requests.createDatabase(hosts[0], db));
        }

    }
}
