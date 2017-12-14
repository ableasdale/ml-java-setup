import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.Security;

public class TestSSH {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        Util.JcePolicyFix();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        SSHClient client = new SSHClient();
        client.addHostKeyVerifier(new PromiscuousVerifier());

        try {
            client.connect(Util.getConfiguration().getString("host"));
        } catch (IOException e) {
            LOG.error("IOException: ", e);
        }

        PKCS8KeyFile keyFile = new PKCS8KeyFile();
        keyFile.init(new File(Util.getConfiguration().getString("pemfile")));
        try {
            client.authPublickey(Util.getConfiguration().getString("sshuser"), keyFile);
        } catch (UserAuthException e) {
            LOG.error("UserAuthException: ", e);
        } catch (TransportException e) {
            LOG.error("TransportException: ", e);
        }


        LOG.info(Util.execCmd(client, "whoami"));
       // LOG.info(Util.execCmd(client, "ls -lart"));
       // LOG.info(Util.execCmd(client, "ps aux"));
       // LOG.info(Util.execCmd(client, "/usr/sbin/service MarkLogic status"));
       // LOG.info(Util.execCmd(client, "/usr/local/sbin/mladmin"));
        LOG.info(Util.execCmd(client, "sudo /usr/sbin/service MarkLogic pstack"));
    }
}
