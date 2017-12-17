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

public class MultiNodeClusterSetup {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
            // LOG.info(sshcc.getHostName());

            sshcc.setClient(Util.initializeHost(s));

            // Initial configuration checks
            //LOG.info(com.marklogic.support.utilmarklogic.support.Util.execCmd(sshcc.getClient(), "uname -a"));
            //LOG.info(com.marklogic.support.Utilogic.support.util.Util.execCmd(sshcc.getClient(), "/usr/sbin/service MarkLogic status"));
            //LOG.info(com.marklogic.support.Utilogic.support.util.Util.execCmd(sshcc.getClient(), "cat /proc/meminfo | grep AnonHugePages"));

            // Completely clean all MarkLogic data on each of the three hosts
            LOG.debug(Util.execSudoCmd(sshcc, "/usr/sbin/service MarkLogic stop"));
            LOG.debug(Util.execSudoCmd(sshcc, "rm -rf /var/opt/MarkLogic"));
            LOG.debug(Util.execSudoCmd(sshcc, "/usr/sbin/service MarkLogic start"));

            //LOG.info(com.marklogic.support.Utilogic.support.util.Util.execCmd(sshcc.getClient(), "/usr/sbin/service MarkLogic stop"));

            clientConnectionList.add(sshcc);
            Util.closeSSHClient(sshcc.getClient());
            // Initialise MarkLogic on (each) node
            LOG.info("Running /admin/v1/init on host " + s);
            Response response = Util.processHttpRequest(Requests.initMarkLogicNode(s));
            LOG.debug(String.format("Response Code: %d", response.code()));
        }

        // Part Two - initialize the primary node using the MarkLogic ReST API
        Util.processHttpRequest(Requests.configurePrimaryNode(hosts[0]));
        LOG.info(String.format("Master host (%s) should now be configured with the default databases", hosts[0]));
        /* Placeholder until I write code to do the timestamp polling request... */
        try {
            Thread.sleep(2000);
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
