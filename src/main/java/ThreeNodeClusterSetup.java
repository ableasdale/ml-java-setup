import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
            LOG.info(sshcc.getHostName());

            sshcc.setClient(Util.initializeHost(s));

            // Initial configuration checks
            LOG.info(Util.execCmd(sshcc.getClient(), "uname -a"));
            //LOG.info(Util.execCmd(sshcc.getClient(), "/usr/sbin/service MarkLogic status"));
            //LOG.info(Util.execCmd(sshcc.getClient(), "cat /proc/meminfo | grep Anon"));

            // Completely clean all MarkLogic data on each of the three hosts
            LOG.info(Util.execSudoCmd(sshcc, "/usr/sbin/service MarkLogic stop"));
            LOG.info(Util.execSudoCmd(sshcc, "rm -rf /var/opt/MarkLogic"));
            LOG.info(Util.execSudoCmd(sshcc, "/usr/sbin/service MarkLogic start"));

            //LOG.info(Util.execCmd(sshcc.getClient(), "/usr/sbin/service MarkLogic stop"));

            clientConnectionList.add(sshcc);
            Util.closeSSHClient(sshcc.getClient());

            // Initialise MarkLogic on (each) node
            Response response = Util.processHttpRequest(Requests.initMarkLogicNode(sshcc.getHostName()));
            LOG.info(String.format("Response Code: %d", response.code()));
        }

        // Part Two - initialize the primary node using the MarkLogic ReST API
        Response response = Util.processHttpRequest(Requests.configurePrimaryNode(hosts[0]));
        LOG.info(String.format("Response Code: %d", response.code()));

        /* Placeholder until I write code to do the timestamp polling request... */
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Part Three - join the second node to the master
        //response = Util.processHttpRequest(Requests.joinBootstrapHost(hosts[0], hosts[1]));

        // 3a:
        String joinerData = Util.processHttpRequestAndGetBody(Requests.getJoinerHostConfiguration(hosts[1]));
        LOG.debug("DATA:"+ joinerData);

        // 3b:
        byte[] joinerFullConfiguration = Util.processHttpRequestAndGetBodyAsByteArray(Requests.joinBootstrapHost(hosts[0], joinerData));
        LOG.debug("DATA:"+ joinerFullConfiguration);

        // 3c:
        String joinedConfiguration = Util.processHttpRequestAndGetBody(Requests.joinTargetHostToCluster(hosts[1], joinerFullConfiguration));
        LOG.info("DATA:"+ joinedConfiguration);

        // Part Four - join the third node to the master


       /* $CURL -X POST -H "Content-type: application/zip" \
        --data-binary @./cluster-config.zip \
        http://${JOINING_HOST}:8001/admin/v1/cluster-config \ */

        //LOG.info(String.format("Response Code: %d", response.code()));




        //response = Util.processHttpRequest(Requests.joinPrimaryNode(hosts[2]));
        //LOG.info(String.format("Response Code: %d", response.code()));


        /*
        request = new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/instance-admin", hosts[0]))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), String.format("admin-username=%s&admin-password=%s&realm=public", Util.getConfiguration().getString("mluser"), Util.getConfiguration().getString("mlpass"))))
                .build();

        response = Util.processHttpRequest(request);
        LOG.info(String.format("Response Code: %d", response.code())); */


        /* Placeholder until I write code to do the timestamp polling request...
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /// 2. wait for timestamp
        // /admin/v1/timestamp

        request = new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/timestamp", hosts[0]))
                .get()
                .build();

        response = Util.processHttpRequest(request);
        LOG.info(String.format("Response Code: %d", response.code()));
        //LOG.info(response.body().string()); */

        // 3. Setup initial DBs

        /* curl -v --trace-ascii out.txt -X POST -H "Content-type: application/x-www-form-urlencoded" \
   --data "admin-username=q&admin-password=q&realm=public" \
   http://host:8001/admin/v1/instance-admin */

        //LOG.info(response.body().toString());


        /* Part Three - join the second node to the master
        request = new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/init", hosts[0]))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), ""))
                .build();

        response = Util.processHttpRequest(request);
        LOG.info(String.format("Response Code: %d", response.code()));
*/

    }
}
