import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class ThreeNodeClusterSetup {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        Util.jcePolicyFix();
        String[] hosts = Util.getConfiguration().getStringArray("hosts");
        List<SSHClientConnection> clientConnectionList = new ArrayList<>();

        // Part One
        for (String s : hosts) {
            // TODO - can this be done in 3 concurrent threads to speed things up.
            SSHClientConnection sshcc = new SSHClientConnection();
            sshcc.setName(s);
            // LOG.info(sshcc.getHostName());

            sshcc.setClient(Util.initializeHost(s));

            // Initial configuration checks
            //LOG.info(Util.execCmd(sshcc.getClient(), "uname -a"));
            //LOG.info(Util.execCmd(sshcc.getClient(), "/usr/sbin/service MarkLogic status"));
            //LOG.info(Util.execCmd(sshcc.getClient(), "cat /proc/meminfo | grep AnonHugePages"));

            // Completely clean all MarkLogic data on each of the three hosts
            LOG.info(Util.execSudoCmd(sshcc, "/usr/sbin/service MarkLogic stop"));
            LOG.info(Util.execSudoCmd(sshcc, "rm -rf /var/opt/MarkLogic"));
            LOG.info(Util.execSudoCmd(sshcc, "/usr/sbin/service MarkLogic start"));

            //LOG.info(Util.execCmd(sshcc.getClient(), "/usr/sbin/service MarkLogic stop"));

            clientConnectionList.add(sshcc);
            Util.closeSSHClient(sshcc.getClient());
            // Initialise MarkLogic on (each) node
            LOG.debug("Running /admin/v1/init on host " + s);
            Response response = Util.processHttpRequest(Requests.initMarkLogicNode(s));
            LOG.debug(String.format("Response Code: %d", response.code()));
        }

        // Part Two - initialize the primary node using the MarkLogic ReST API
        Response response = Util.processHttpRequest(Requests.configurePrimaryNode(hosts[0]));
        LOG.info(String.format("Response Code: %d", response.code()));
        LOG.info("First host should now be configured");
        /* Placeholder until I write code to do the timestamp polling request... */
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Part Three - join the second node to the master
        // 3a:
        byte[] joinerData = Util.processHttpRequestAndGetBodyAsByteArray(Requests.getJoinerHostConfiguration(hosts[1]));
        // 3b:
        byte[] joinerFullConfiguration = Util.processHttpRequestAndGetBodyAsByteArray(Requests.joinBootstrapHost(hosts[0], joinerData));
        // 3c:
        Util.processHttpRequestAndGetBody(Requests.joinTargetHostToCluster(hosts[1], joinerFullConfiguration));

        // Part Four - join the third node to the master
        byte[] hostJoinerData = Util.processHttpRequestAndGetBodyAsByteArray(Requests.getJoinerHostConfiguration(hosts[2]));
        byte[] clusterConfiguration = Util.processHttpRequestAndGetBodyAsByteArray(Requests.joinBootstrapHost(hosts[0], hostJoinerData));
        Util.processHttpRequestAndGetBody(Requests.joinTargetHostToCluster(hosts[2], clusterConfiguration));

    }
}
