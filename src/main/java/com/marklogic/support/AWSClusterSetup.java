package com.marklogic.support;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import com.marklogic.support.util.MarkLogicConfig;
import com.marklogic.support.util.Requests;
import com.marklogic.support.util.Util;
import com.marklogic.support.util.XQueryBuilder;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AWSClusterSetup {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        final DigestAuthenticator authenticator = new DigestAuthenticator(new Credentials(Util.getConfiguration().getString("mluser"), Util.getConfiguration().getString("mlpass")));
        final Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();
        OkHttpClient HTTPCLIENT = new OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.MINUTES)
                .writeTimeout(90, TimeUnit.MINUTES)
                .readTimeout(90, TimeUnit.MINUTES)
                .authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
                .addInterceptor(new AuthenticationCacheInterceptor(authCache))
                .build();



        String[] hosts = Util.getConfiguration().getStringArray("hosts");
        LOG.info(String.format("Running /admin/v1/init on host %s", hosts[0]));
        Response response = Util.processHttpRequest(Requests.initMarkLogicNode(hosts[0]));
        LOG.info(String.format("Response Code: %d", response.code()));
        // Part Two - Initialize the primary node using the MarkLogic ReST API
        // Util.processHttpRequest(Requests.configurePrimaryNode(hosts[0]));

        Request r = new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/instance-admin", hosts[0]))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), String.format("admin-username=%s&admin-password=%s&realm=public", Util.getConfiguration().getString("mluser"), Util.getConfiguration().getString("mlpass"))))
                .build();


        try {
            HTTPCLIENT.newCall(r).execute();
        } catch (IOException e) {
            LOG.error("exception", e);
        }


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
            LOG.info(String.format("Running /admin/v1/init on host %s", hosts[i]));
            Response response2 = Util.processHttpRequest(Requests.initMarkLogicNode(hosts[i]));
            LOG.info(String.format("Response Code: %d", response2.code()));
            MarkLogicConfig.addHostToCluster(hosts[i], hosts[0]);
        }
    }
}
