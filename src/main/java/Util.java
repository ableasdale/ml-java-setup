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
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.Security;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.matcher.Matchers.contains;

public class Util {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static SSHClient CLIENT = null;
    private static Configuration CONFIG = null;
    private static String HOSTNAME = Util.getConfiguration().getString("host").substring(0, Util.getConfiguration().getString("host").indexOf("."));

    protected static void JcePolicyFix() {
        try {
            // Hack for JCE Unlimited Strength
            // See: https://stackoverflow.com/questions/3425766/how-would-i-use-maven-to-install-the-jce-unlimited-strength-policy-files
            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, false);
        } catch (IllegalAccessException e) {
            LOG.error("IllegalAccessException: ", e);
        } catch (ClassNotFoundException e) {
            LOG.error("ClassNotFoundException: ", e);
        } catch (NoSuchFieldException e) {
            LOG.error("NoSuchFieldException: ", e);
        }
    }

    protected static SSHClient getSSHClient() {
        if (CLIENT != null) {
            return CLIENT;
        } else {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            CLIENT = new SSHClient();
            CLIENT.addHostKeyVerifier(new PromiscuousVerifier());
            try {
                CLIENT.connect(Util.getConfiguration().getString("host"));
            } catch (IOException e) {
                LOG.error("IOException: ", e);
            }
            PKCS8KeyFile keyFile = new PKCS8KeyFile();
            keyFile.init(new File(Util.getConfiguration().getString("pemfile")));
            try {
                CLIENT.authPublickey(Util.getConfiguration().getString("sshuser"), keyFile);
            } catch (UserAuthException e) {
                LOG.error("UserAuthException: ", e);
            } catch (TransportException e) {
                LOG.error("TransportException: ", e);
            }
            return CLIENT;
        }
    }

    protected static void closeSSHClient() {
        if (CLIENT != null) {
            try {
                CLIENT.close();
                CLIENT = null;
            } catch (IOException e) {
                LOG.error("IOException: ", e);
            }
        }
    }

    protected static String execCmd(SSHClient client, String cmd) {
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

    protected static Configuration getConfiguration() {
        if (CONFIG != null) {
            return CONFIG;
        } else {
            LOG.info("trying to get configuration for the first time");
            Configurations configs = new Configurations();
            try {
                CONFIG = configs.properties(new File("config.properties"));
            } catch (ConfigurationException cex) {
                LOG.error("Configuration Exception: ", cex);
            }
            return CONFIG;
        }
    }
}
