package com.marklogic.support.beans;

import net.schmizz.sshj.SSHClient;

public class SSHClientConnection {

    private String name;
    private SSHClient client = null;
    private boolean isInitialized = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostName() {
        return name.substring(0, name.indexOf("."));
    }

    public SSHClient getClient() {
        return client;
    }

    public void setClient(SSHClient client) {
        this.client = client;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    private void setInitialized(boolean initialized) {
        isInitialized = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SSHClientConnection that = (SSHClientConnection) o;

        if (isInitialized != that.isInitialized) return false;
        if (!name.equals(that.name)) return false;
        return client.equals(that.client);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + client.hashCode();
        result = 31 * result + (isInitialized ? 1 : 0);
        return result;
    }
}
