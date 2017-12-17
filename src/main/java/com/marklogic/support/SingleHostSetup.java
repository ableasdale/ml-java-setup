package com.marklogic.support;

import com.marklogic.support.util.Util;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class SingleHostSetup {


    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        OkHttpClient okhc = Util.getHttpClient();

        // 1. Init
        // curl --anyauth -X POST -d "" -i http://host:8001/admin/v1/init

        Request request = new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/init", Util.getConfiguration().getString("host")))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), ""))
                .build();

        Response response = Util.processHttpRequest(request);
        LOG.info(String.format("Response Code: %d", response.code()));
        //LOG.info(response.body().string());

        /// 2. wait for timestamp
        // /admin/v1/timestamp

        request = new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/timestamp", Util.getConfiguration().getString("host")))
                .get()
                .build();

        response = Util.processHttpRequest(request);
        LOG.info(String.format("Response Code: %d", response.code()));
        //LOG.info(response.body().string());

        // 3. Setup initial DBs

        /* curl -v --trace-ascii out.txt -X POST -H "Content-type: application/x-www-form-urlencoded" \
   --data "admin-username=q&admin-password=q&realm=public" \
   http://host:8001/admin/v1/instance-admin */

        request = new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/instance-admin", Util.getConfiguration().getString("host")))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), String.format("admin-username=%s&admin-password=%s&realm=public", Util.getConfiguration().getString("mluser"), Util.getConfiguration().getString("mlpass"))))
                .build();

        response = Util.processHttpRequest(request);
        LOG.info(String.format("Response Code: %d", response.code()));
        //LOG.info(response.body().toString());

    }
}
