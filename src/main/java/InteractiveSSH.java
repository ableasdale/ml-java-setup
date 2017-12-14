import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.Security;

import static net.sf.expectit.matcher.Matchers.contains;

public class InteractiveSSH {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        String hostname = Util.getConfiguration().getString("host").substring(0, Util.getConfiguration().getString("host").indexOf("."));

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

        try {

            Session session;
            Shell shell;
            session=client.startSession();
            session.allocateDefaultPTY();
            shell=session.startShell();
            Expect expect = new ExpectBuilder()
                    .withOutput(shell.getOutputStream())
                    .withInputs(shell.getInputStream(), shell.getErrorStream())
                    .build();

            expect.sendLine("sudo /usr/sbin/service MarkLogic pstack");
            //expect.sendLine("sudo ls -lart");
            expect.expect(contains(String.format("[sudo] password for %s:", Util.getConfiguration().getString("sshuser"))));
            expect.sendLine(Util.getConfiguration().getString("supasswd"));

            String list = expect.expect(contains(String.format("[%s@%s ~]$", Util.getConfiguration().getString("sshuser"), hostname))).getBefore();

            LOG.info("PStack: " + list);

            /*expect.sendLine("sudo passwd "+uname);
            expect.sendLine(pwd);
            statusbar.setText("Assigning access key to user...");
            expect.sendLine("sudo mkdir /home/"+uname+"/.ssh");
            expect.sendLine("sudo touch /home/"+uname+"/.ssh/authorized_keys");
            expect.sendLine("sudo echo "+pemfile+">/home/"+uname+"/.ssh/authorized_keys");
            statusbar.setText("Providing permissions to user...");
            expect.sendLine("sudo chown root /home/"+uname);
            expect.sendLine("sudo chmod go-w /home/"+uname);
            expect.sendLine("sudo mkdir /home/"+uname+"/"+uname);
            expect.sendLine("sudo chmod ug+rwX /home/"+uname);
            expect.sendLine("sudo chmod 700 /home/"+uname+"/.ssh");
            expect.sendLine("sudo chmod 600 /home/"+uname+"/.ssh/authorized_keys");
            expect.sendLine("sudo chmod 755 /home/"+uname);
            statusicon.setForeground(Color.green);
            statusbar.setText("User created!");*/
            expect.close();



            /*
            Session session = client.startSession();
            session.allocateDefaultPTY();
            Session.Command command = session.exec(cmd);
            response = IOUtils.readFully(command.getInputStream()).toString();
            command.join(10, TimeUnit.SECONDS);*/
            //LOG.info(response);
            session.close();
        } catch (ConnectionException e) {
            LOG.error("ConnectionException: ", e);
        } catch (TransportException e) {
            LOG.error("TransportException: ", e);
        } catch (IOException e) {
            LOG.error("IOException: ", e);
        }

        //LOG.info(Util.execCmd(client, "whoami"));

    }
}
