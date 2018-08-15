package com.marklogic.support;

import com.marklogic.support.util.MarkLogicConfig;
import com.marklogic.support.util.Requests;
import com.marklogic.support.util.Util;
import com.marklogic.support.util.XQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class AWSClusterSetup {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        String[] hosts = Util.getConfiguration().getStringArray("hosts");
        // Part Two - Initialize the primary node using the MarkLogic ReST API
        Util.processHttpRequest(Requests.configurePrimaryNode(hosts[0]));
        LOG.info(String.format("Master host (%s) should now be configured with the default databases", hosts[0]));

        /* Placeholder until I write code to do the /v1/timestamp polling request... */
        try {
            // TODO - this may well be way too short!
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            LOG.error("Exception caught: ", e);
        }

        // Set group level logging to debug : admin:group-set-file-log-level($config, $groupid, "debug")
        //Util.processHttpRequest(Requests.evaluateXQuery(hosts[0], XQueryBuilder.configureBaseGroupSettings(backgroundIOLimit)));

        // Part Three - Join all additional nodes to the master host
        for (int i = 1; i < hosts.length; i++) {
            MarkLogicConfig.addHostToCluster(hosts[i], hosts[0]);
        }
    }
}
