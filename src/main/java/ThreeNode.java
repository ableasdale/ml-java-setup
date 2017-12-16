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

        for (String s: hosts){
            SSHClientConnection sshcc = new SSHClientConnection();
            sshcc.setName(s);
            LOG.info(sshcc.getHostName());

            sshcc.setClient(Util.initializeHost(s));
            LOG.info(Util.execCmd(sshcc.getClient(), "uname -a"));
            LOG.info(Util.execCmd(sshcc.getClient(), "/usr/sbin/service MarkLogic status"));
            LOG.info(Util.execCmd(sshcc.getClient(), "cat /proc/meminfo | grep Anon"));

            clientConnectionList.add(sshcc);
            Util.closeSSHClient(sshcc.getClient());
        }

    }
}
