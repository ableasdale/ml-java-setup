import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class ThreeNode {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        Util.jcePolicyFix();
        String[] hosts = Util.getConfiguration().getStringArray("hosts");

        List<SSHClientConnection> clientConnectionList = new ArrayList<>();

        // Part One
        for (String s: hosts){
            SSHClientConnection sshcc = new SSHClientConnection();
            sshcc.setName(s);
            LOG.info(sshcc.getHostName());

            sshcc.setClient(Util.initializeHost(s));

            // Initial configuration checks
            LOG.info(Util.execCmd(sshcc.getClient(), "uname -a"));
            LOG.info(Util.execCmd(sshcc.getClient(), "/usr/sbin/service MarkLogic status"));
            LOG.info(Util.execCmd(sshcc.getClient(), "cat /proc/meminfo | grep Anon"));

            // Completely clean all MarkLogic data
            LOG.info(Util.execSudoCmd(sshcc, "/usr/sbin/service MarkLogic stop"));
            LOG.info(Util.execSudoCmd(sshcc, "rm -rf /var/opt/MarkLogic"));
            LOG.info(Util.execSudoCmd(sshcc, "/usr/sbin/service MarkLogic start"));

            //LOG.info(Util.execCmd(sshcc.getClient(), "/usr/sbin/service MarkLogic stop"));

            clientConnectionList.add(sshcc);
            Util.closeSSHClient(sshcc.getClient());
        }

        // Part Two - initialize first node using calls to the MarkLogic ReST API

    }
}
