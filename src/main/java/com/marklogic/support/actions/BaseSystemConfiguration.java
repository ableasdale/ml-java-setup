package com.marklogic.support.actions;

import com.marklogic.support.beans.SSHClientConnection;
import com.marklogic.support.util.Requests;
import com.marklogic.support.util.Util;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class BaseSystemConfiguration implements Runnable {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private SSHClientConnection sshcc;

    public BaseSystemConfiguration(SSHClientConnection sshcc) {
        this.sshcc = sshcc;
    }

    @Override
    public void run() {
        // Initial configuration checks
        LOG.info(Util.execCmd(sshcc.getClient(), "uname -a"));
        //LOG.info(Util.execCmd(sshcc.getClient(), "/usr/sbin/service MarkLogic status"));
        LOG.info(Util.execCmd(sshcc.getClient(), "cat /proc/meminfo | grep AnonHugePages"));

        // Completely clean all MarkLogic data on each of the three hosts
        LOG.debug(Util.execSudoCmd(sshcc, "/usr/sbin/service MarkLogic stop"));
        LOG.debug(Util.execSudoCmd(sshcc, "rm -rf /var/opt/MarkLogic"));
        LOG.debug(Util.execSudoCmd(sshcc, String.format("rm -rf %s/Forests", Util.getConfiguration().getString("datadirectory"))));
        LOG.debug(Util.execSudoCmd(sshcc, String.format("rm -rf %s/*", Util.getConfiguration().getString("backupdirectory"))));
        LOG.debug(Util.execSudoCmd(sshcc, "/usr/sbin/service MarkLogic start"));

        // Test: dump out pstack to make sure we're really working
        // LOG.info(Util.execSudoCmd(sshcc, "/usr/sbin/service MarkLogic pstack"));

        Util.closeSSHClient(sshcc.getClient());
        // Initialise MarkLogic on (each) node
        LOG.info(String.format("Running /admin/v1/init on host %s", sshcc.getName()));
        Response response = Util.processHttpRequest(Requests.initMarkLogicNode(sshcc.getName()));
        LOG.debug(String.format("Response Code: %d", response.code()));
    }
}
