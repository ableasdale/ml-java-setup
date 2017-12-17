package com.marklogic.support.util;

public class MarkLogicConfig {

    public static void addHostToCluster(String joinerHostname, String bootstrapHostname) {
        byte[] joinerData = Util.processHttpRequestAndGetBodyAsByteArray(Requests.getJoinerHostConfiguration(joinerHostname));
        byte[] joinerFullConfiguration = Util.processHttpRequestAndGetBodyAsByteArray(Requests.joinBootstrapHost(bootstrapHostname, joinerData));
        Util.processHttpRequestAndGetBody(Requests.joinTargetHostToCluster(joinerHostname, joinerFullConfiguration));
    }
}
