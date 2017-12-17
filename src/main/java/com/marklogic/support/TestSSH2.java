package com.marklogic.support;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;

import java.io.File;
import java.io.IOException;
import java.security.Security;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;

public class TestSSH2 {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        /*
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        PKCS8KeyFile keyFile = new PKCS8KeyFile();
        keyFile.init(new File("/Users/ableasdale/.ssh/id_rsa.pem"));

        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts();
        ssh.connect("engrlab-128-115.engrlab.marklogic.com");
        try {
            ssh.authPublickey(System.getProperty("user.name"));

            // Present here to demo algorithm renegotiation - could have just put this before connect()
            // Make sure JZlib is in classpath for this to work
            ssh.useCompression();

            final String src = System.getProperty("user.home") + File.separator + "test_file";
            ssh.newSCPFileTransfer().upload(new FileSystemFile(src), "/tmp/");
        } finally {
            ssh.disconnect();
        } */

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        SSHClient client = new SSHClient();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.connect("engrlab-128-115.engrlab.marklogic.com");

        PKCS8KeyFile keyFile = new PKCS8KeyFile();
        keyFile.init(new File("/Users/ableasdale/.ssh/id_rsa.pem"));
        try {
            client.authPublickey("ableasdale", keyFile);
        } catch (UserAuthException e) {
            e.printStackTrace();
        } catch (TransportException e) {
            e.printStackTrace();
        }


        Session session = client.startSession();
        session.allocateDefaultPTY();
        Session.Shell shell = session.startShell();
        Expect expect = new ExpectBuilder()
                .withOutput(shell.getOutputStream())
                .withInputs(shell.getInputStream(), shell.getErrorStream())
                .build();
        try {
            expect.expect(contains("[RETURN]"));
            expect.sendLine();
            String ipAddress = expect.expect(regexp("Trying (.*)\\.\\.\\.")).group(1);
            System.out.println("Captured IP: " + ipAddress);
            expect.expect(contains("login:"));
            expect.sendLine("new");
            expect.expect(contains("(Y/N)"));
            expect.send("N");
            expect.expect(regexp(": $"));
            expect.send("\b");
            expect.expect(regexp("\\(y\\/n\\)"));
            expect.sendLine("y");
            expect.expect(contains("Would you like to sign the guestbook?"));
            expect.send("n");
            expect.expect(contains("[RETURN]"));
            expect.sendLine();
        } finally {
            session.close();
            client.close();
            expect.close();
        }


    }
}
