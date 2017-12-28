package com.marklogic.support.util;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Requests {

    public static Request initMarkLogicNode(String hostname) {
        return new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/init", hostname))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), ""))
                .build();
    }

    public static Request configurePrimaryNode(String hostname) {
        return new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/instance-admin", hostname))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), String.format("admin-username=%s&admin-password=%s&realm=public", Util.getConfiguration().getString("mluser"), Util.getConfiguration().getString("mlpass"))))
                .build();
    }

    /* Join Cluster 1. `$CURL -X GET -H "Accept: application/xml" http://${JOINING_HOST}:8001/admin/v1/server-config` */
    public static Request getJoinerHostConfiguration(String joinerHost) {
        return new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/server-config", joinerHost))
                .header("Accept", "application/xml")
                .get()
                .build();
    }

    /* Join Cluster 2. $AUTH_CURL -X POST -o cluster-config.zip -d "group=Default" \
        --data-urlencode "server-config=${JOINER_CONFIG}" \
            -H "Content-type: application/x-www-form-urlencoded" \
    http://${BOOTSTRAP_HOST}:8001/admin/v1/cluster-config */
    public static Request joinBootstrapHost(String bootstrapHost, byte[] joinerConfiguration) {
        Request r = null;
        try {
            r = new Request.Builder()
                    .url(String.format("http://%s:8001/admin/v1/cluster-config", bootstrapHost))
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), String.format("group=Default&server-config=%s", URLEncoder.encode(new String(joinerConfiguration), "UTF-8"))))
                    .build();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return r;
    }

    /* Join Cluster 3. $CURL -X POST -H "Content-type: application/zip" \
        --data-binary @./cluster-config.zip \
        http://${JOINING_HOST}:8001/admin/v1/cluster-config \ */
    public static Request joinTargetHostToCluster(String joiningHost, byte[] joinerZipConfiguration) {
        return new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/cluster-config", joiningHost))
                .header("Accept", "application/zip")
                .post(RequestBody.create(MediaType.parse("application/zip"), joinerZipConfiguration))
                .build();
    }

    /* Create a database */
    /* curl --anyauth --user admin:admin -i -X POST -d'"{"rest-api":{"name":"PrimaryApplication"}}"' -H "Content-type: application/json" http://localhost:8002/LATEST/rest-apis */
    public static Request createDatabase(String evalHost, String databaseName) {
        return new Request.Builder()
                .url(String.format("http://%s:8002/LATEST/rest-apis", evalHost))
                .header("Accept", "application/json")
                .post(RequestBody.create(MediaType.parse("application/json"), String.format("{\"rest-api\":{\"name\":\"%s\", \"forests-per-host\": %d }}", databaseName, 1)))
                .build();
    }

    /* Create Database, Forests and Replicas */
    /* curl --anyauth --user user:password -X POST -i -d @./body.xqy \
    -H "Content-type: application/x-www-form-urlencoded" \
    -H "Accept: multipart/mixed; boundary=BOUNDARY" \
    http://localhost:8000/v1/eval */
    public static Request createDatabaseForestsAndReplicas(String bootstrapHost, String encodedXquery) {
        return new Request.Builder()
                .url(String.format("http://%s:8000/v1/eval", bootstrapHost))
                .header("Accept", "multipart/mixed; boundary=BOUNDARY\"")
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), encodedXquery))
                .build();
    }

}
