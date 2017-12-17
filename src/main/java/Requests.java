import okhttp3.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Requests {

    protected static Request initMarkLogicNode(String hostname){
        return new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/init", hostname))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), ""))
                .build();
    }

    protected static Request configurePrimaryNode(String hostname) {
        return new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/instance-admin", hostname))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), String.format("admin-username=%s&admin-password=%s&realm=public", Util.getConfiguration().getString("mluser"), Util.getConfiguration().getString("mlpass"))))
                .build();
    }

    /* Join Cluster 1. `$CURL -X GET -H "Accept: application/xml" http://${JOINING_HOST}:8001/admin/v1/server-config` */
    protected static Request getJoinerHostConfiguration(String joinerHost) {
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

    protected static Request joinBootstrapHost(String bootstrapHost, byte[] joinerConfiguration) {
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
    protected static Request joinTargetHostToCluster(String joiningHost, byte[] joinerZipConfiguration) {
        return new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/cluster-config", joiningHost))
                .header("Accept", "application/zip")
                .post(RequestBody.create(MediaType.parse("application/zip"), joinerZipConfiguration))
                .build();
    }

}
