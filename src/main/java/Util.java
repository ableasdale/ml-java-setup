import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
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
import java.util.concurrent.TimeUnit;

public class Util {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static Configuration CONFIG = null;

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


    protected static Configuration getConfiguration() {
        if (CONFIG != null) {
            return CONFIG;
        } else {
            LOG.info("trying to get config!");
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
