package com.marklogic.support.util;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import com.marklogic.support.beans.SSHClientConnection;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import okhttp3.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.matcher.Matchers.contains;

public class Util {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static SSHClient SSHCLIENT = null;
    private static OkHttpClient HTTPCLIENT = null;
    private static Configuration CONFIG = null;
    private static String HOSTNAME = Util.getConfiguration().getString("host").substring(0, Util.getConfiguration().getString("host").indexOf("."));

    public static OkHttpClient getHttpClient(){
        if (HTTPCLIENT != null) {
            return HTTPCLIENT;
        } else {
            final DigestAuthenticator authenticator = new DigestAuthenticator(new Credentials(Util.getConfiguration().getString("mluser"), Util.getConfiguration().getString("mlpass")));
            final Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();
            HTTPCLIENT = new OkHttpClient.Builder()
                    .connectTimeout(90, TimeUnit.MINUTES)
                    .writeTimeout(90, TimeUnit.MINUTES)
                    .readTimeout(90, TimeUnit.MINUTES)
                    .authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
                    .addInterceptor(new AuthenticationCacheInterceptor(authCache))
                    .build();
            return HTTPCLIENT;
        }
    }

    public static Response processHttpRequest(Request r) {
        LOG.debug(String.format("URL: %s", r.url()));
        Response response = null;
        try {
            response = getHttpClient().newCall(r).execute();
            LOG.debug(String.format("%s | %s", response.toString(), response.body().string()));
            response.close();
        } catch (IOException e) {
            LOG.error("Exception caught creating resource: ", e);
        }
        return response;
    }

    public static String processHttpRequestAndGetBody(Request r) {
        LOG.debug(String.format("URL: %s", r.url()));
        Response response = null;
        String responseData = null;
        try {
            response = getHttpClient().newCall(r).execute();
            //LOG.debug(String.format("%s | %s", response.toString(), response.body().string()));
            ResponseBody body = response.body();
            //MultipartBody.Part part1 = body.part(0);
            responseData = response.body().string();
            response.close();
        } catch (IOException e) {
            LOG.error("Exception caught creating resource: ", e);
        }
        return responseData;
    }

    protected static byte[] processHttpRequestAndGetBodyAsByteArray(Request r) {
        LOG.debug(String.format("URL: %s", r.url()));
        Response response = null;
        byte[] responseData = null;
        try {
            response = getHttpClient().newCall(r).execute();
            //LOG.debug(String.format("%s | %s", response.toString(), response.body().string()));
            responseData = response.body().bytes();
            response.close();
        } catch (IOException e) {
            LOG.error("Exception caught creating resource: ", e);
        }
        return responseData;
    }

    public static void jcePolicyFix() {
        try {
            // Hack for JCE Unlimited Strength
            // See: https://stackoverflow.com/questions/3425766/how-would-i-use-maven-to-install-the-jce-unlimited-strength-policy-files
            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, false);

            // Add the BouncyCastle Security Provider - one time
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (IllegalAccessException e) {
            LOG.error("IllegalAccessException: ", e);
        } catch (ClassNotFoundException e) {
            LOG.error("ClassNotFoundException: ", e);
        } catch (NoSuchFieldException e) {
            LOG.error("NoSuchFieldException: ", e);
        }
    }

    public static SSHClient initializeHost(String hostname) {
        SSHClient c = new SSHClient();
        c.addHostKeyVerifier(new PromiscuousVerifier());
        try {
            c.connect(hostname);
        } catch (IOException e) {
            LOG.error("IOException: ", e);
        }
        PKCS8KeyFile keyFile = new PKCS8KeyFile();
        keyFile.init(new File(Util.getConfiguration().getString("pemfile")));
        try {
            c.authPublickey(Util.getConfiguration().getString("sshuser"), keyFile);
        } catch (UserAuthException e) {
            LOG.error("UserAuthException: ", e);
        } catch (TransportException e) {
            LOG.error("TransportException: ", e);
        }
        return c;
    }


    public static SSHClient getSSHClient() {
        if (SSHCLIENT != null) {
            return SSHCLIENT;
        } else {
            SSHCLIENT = new SSHClient();
            SSHCLIENT.addHostKeyVerifier(new PromiscuousVerifier());
            try {
                SSHCLIENT.connect(Util.getConfiguration().getString("host"));
            } catch (IOException e) {
                LOG.error("IOException: ", e);
            }
            PKCS8KeyFile keyFile = new PKCS8KeyFile();
            keyFile.init(new File(Util.getConfiguration().getString("pemfile")));
            try {
                SSHCLIENT.authPublickey(Util.getConfiguration().getString("sshuser"), keyFile);
            } catch (UserAuthException e) {
                LOG.error("UserAuthException: ", e);
            } catch (TransportException e) {
                LOG.error("TransportException: ", e);
            }
            return SSHCLIENT;
        }
    }

    public static void closeSSHClient() {
        if (SSHCLIENT != null) {
            try {
                SSHCLIENT.close();
                SSHCLIENT = null;
            } catch (IOException e) {
                LOG.error("IOException: ", e);
            }
        }
    }

    public static void closeSSHClient(SSHClient c) {
        if (c != null) {
            try {
                c.close();
                c = null;
            } catch (IOException e) {
                LOG.error("IOException: ", e);
            }
        }
    }

    public static String execCmd(SSHClient client, String cmd) {
        String response = null;
        try {
            Session session = client.startSession();
            session.allocateDefaultPTY();
            Session.Command command = session.exec(cmd);
            response = IOUtils.readFully(command.getInputStream()).toString();
            command.join(10, TimeUnit.SECONDS);
            session.close();
        } catch (ConnectionException e) {
            LOG.error("ConnectionException: ", e);
        } catch (TransportException e) {
            LOG.error("TransportException: ", e);
        } catch (IOException e) {
            LOG.error("IOException: ", e);
        }
        return response;
    }

    public static String execSudoCmd(SSHClientConnection sshcc, String cmd) {
        String response = null;
        try {
            Session session;
            Session.Shell shell;
            session = sshcc.getClient().startSession();
            session.allocateDefaultPTY();
            shell = session.startShell();
            Expect expect = new ExpectBuilder()
                    .withOutput(shell.getOutputStream())
                    .withInputs(shell.getInputStream(), shell.getErrorStream())
                    .build();
            expect.sendLine(String.format("sudo %s", cmd));
            expect.expect(contains(String.format("[sudo] password for %s:", Util.getConfiguration().getString("sshuser"))));
            expect.sendLine(Util.getConfiguration().getString("supasswd"));
            response = expect.expect(contains(String.format("[%s@%s ~]$", Util.getConfiguration().getString("sshuser"), sshcc.getHostName()))).getBefore();
            expect.close();
            session.close();
        } catch (ConnectionException e) {
            LOG.error("ConnectionException: ", e);
        } catch (TransportException e) {
            LOG.error("TransportException: ", e);
        } catch (IOException e) {
            LOG.error("IOException: ", e);
        }
        return response;
    }


    protected static String execSudoCmd(SSHClient client, String cmd) {
        String response = null;
        try {
            Session session;
            Session.Shell shell;
            session = client.startSession();
            session.allocateDefaultPTY();
            shell = session.startShell();
            Expect expect = new ExpectBuilder()
                    .withOutput(shell.getOutputStream())
                    .withInputs(shell.getInputStream(), shell.getErrorStream())
                    .build();
            expect.sendLine(String.format("sudo %s", cmd));
            expect.expect(contains(String.format("[sudo] password for %s:", Util.getConfiguration().getString("sshuser"))));
            expect.sendLine(Util.getConfiguration().getString("supasswd"));
            response = expect.expect(contains(String.format("[%s@%s ~]$", Util.getConfiguration().getString("sshuser"), HOSTNAME))).getBefore();
            expect.close();
            session.close();
        } catch (ConnectionException e) {
            LOG.error("ConnectionException: ", e);
        } catch (TransportException e) {
            LOG.error("TransportException: ", e);
        } catch (IOException e) {
            LOG.error("IOException: ", e);
        }
        return response;
    }

    public static Configuration getConfiguration() {
        if (CONFIG != null) {
            return CONFIG;
        } else {
            LOG.debug("trying to get configuration for the first time");
            try {
                Parameters params = new Parameters();
                FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                        new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                                PropertiesConfiguration.class).configure(params.fileBased()
                                .setListDelimiterHandler(new DefaultListDelimiterHandler(','))
                                .setFile(new File("config.properties")));
                CONFIG = builder.getConfiguration();
            } catch (ConfigurationException cex) {
                LOG.error("Configuration Exception: ", cex);
            }
            return CONFIG;
        }
    }

    public static void loadSampleDataIntoMarkLogic(String[] hosts, String[] databases, String[] databaseStringRangeIndexes) {
        for (String h : hosts) {
            for (String d : databases) {
                Util.processHttpRequest(Requests.evaluateXQuery(h, XQueryDataBuilder.createSampleDocData(d, databaseStringRangeIndexes)));
                //Util.processHttpRequest(Requests.evaluateXQuery(h, XQueryBuilder.evaluateXQueryModuleAgainstDatabase(d)));
            }
        }
    }

    public static void startHttpServer(){
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(new HttpHandler() {

                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
                        exchange.getResponseSender().send(new String(Files.readAllBytes(Paths.get("src/main/resources/google-charts-html"))));
                    }
                }).build();
        server.start();
    }


}
