package com.marklogic.support;

import com.marklogic.support.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class TestSSH {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        Util.jcePolicyFix();

        LOG.info(Util.execCmd(Util.getSSHClient(), "whoami"));
        //LOG.info(com.marklogic.support.utilmarklogic.support.Util.execCmd(com.marklogic.support.utilmarklogic.support.Util.getSSHClient(), "ls -lart"));
        LOG.info(Util.execCmd(Util.getSSHClient(), "ps -ef | grep MarkLogic"));
        LOG.info(Util.execCmd(Util.getSSHClient(), "/usr/sbin/service MarkLogic status"));
        //LOG.info(com.marklogic.support.Utilogic.support.util.Util.execCmd(com.marklogic.support.utilmarklogic.support.Util.getSSHClient(), "/usr/local/sbin/mladmin"));
        // /usr/sbin/service MarkLogic pstack
        // FIXME: LOG.info(Util.execSudoCmd(Util.getSSHClient(), "/usr/sbin/service MarkLogic pstack"));
        // FIXME: LOG.info(Util.execSudoCmd(Util.getSSHClient(), "/usr/sbin/service MarkLogic pmap"));

        Util.closeSSHClient();
    }
}
