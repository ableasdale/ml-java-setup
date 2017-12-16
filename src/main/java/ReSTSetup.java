import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ReSTSetup {

    private static final DigestAuthenticator authenticator = new DigestAuthenticator(new Credentials(Util.getConfiguration().getString("mluser"), Util.getConfiguration().getString("mlpass")));
    private static final Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();
    private static final OkHttpClient okhc = new OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.MINUTES)
            .writeTimeout(90, TimeUnit.MINUTES)
            .readTimeout(90, TimeUnit.MINUTES)
            .authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
            .addInterceptor(new AuthenticationCacheInterceptor(authCache))
            .build();
    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        // 1. Init
        // curl --anyauth -X POST -d "" -i http://host:8001/admin/v1/init

        Request request = new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/init", Util.getConfiguration().getString("host")))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), ""))
                .build();
        try {
            Response response = okhc.newCall(request).execute();
            //LOG.debug(String.format("Cleared Database :: Client Response Status: %d", response.code()));
            LOG.info(String.format("%s | %s", response.toString(), response.body().string()));
        } catch (IOException e) {
            LOG.error("Exception caught creating resource: ", e);
        }

        /// 2. wait for timestamp
        // /admin/v1/timestamp

        request = new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/timestamp", Util.getConfiguration().getString("host")))
                .get()
                .build();
        try {
            Response response = okhc.newCall(request).execute();
            LOG.info(String.format("%s | %s", response.toString(), response.body().string()));
        } catch (IOException e) {
            LOG.error("Exception caught creating resource: ", e);
        }

        // 3. Setup initial DBs

        /* curl -v --trace-ascii out.txt -X POST -H "Content-type: application/x-www-form-urlencoded" \
   --data "admin-username=q&admin-password=q&realm=public" \
   http://host:8001/admin/v1/instance-admin */

        request = new Request.Builder()
                .url(String.format("http://%s:8001/admin/v1/instance-admin", Util.getConfiguration().getString("host")))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), String.format("admin-username=%s&admin-password=%s&realm=public", Util.getConfiguration().getString("mluser"), Util.getConfiguration().getString("mlpass"))))
                .build();
        try {
            Response response = okhc.newCall(request).execute();
            LOG.info(String.format("%s | %s", response.toString(), response.body().string()));
        } catch (IOException e) {
            LOG.error("Exception caught creating resource: ", e);
        }

    }
}
