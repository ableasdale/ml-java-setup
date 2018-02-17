package com.marklogic.support.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BackupStats {

    private String dateTimeOnServer;
    private int totalOverallBackupJobs;
    private int totalCompletedStatus;
    private int totalNotCompletedStatus;
    private String rawOutput;

    public String getDateTimeOnServer() {
        return dateTimeOnServer;
    }

    @XmlElement
    public void setDateTimeOnServer(String dateTimeOnServer) {
        this.dateTimeOnServer = dateTimeOnServer;
    }

    public int getTotalOverallBackupJobs() {
        return totalOverallBackupJobs;
    }

    @XmlElement
    public void setTotalOverallBackupJobs(int totalOverallBackupJobs) {
        this.totalOverallBackupJobs = totalOverallBackupJobs;
    }

    public int getTotalCompletedStatus() {
        return totalCompletedStatus;
    }

    @XmlElement
    public void setTotalCompletedStatus(int totalCompletedStatus) {
        this.totalCompletedStatus = totalCompletedStatus;
    }

    public int getTotalNotCompletedStatus() {
        return totalNotCompletedStatus;
    }

    @XmlElement
    public void setTotalNotCompletedStatus(int totalNotCompletedStatus) {
        this.totalNotCompletedStatus = totalNotCompletedStatus;
    }

    public String getRawOutput() {
        return rawOutput;
    }

    @XmlElement
    public void setRawOutput(String rawOutput) {
        this.rawOutput = rawOutput;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BackupStats that = (BackupStats) o;

        if (totalOverallBackupJobs != that.totalOverallBackupJobs) return false;
        if (totalCompletedStatus != that.totalCompletedStatus) return false;
        if (totalNotCompletedStatus != that.totalNotCompletedStatus) return false;
        if (!dateTimeOnServer.equals(that.dateTimeOnServer)) return false;
        return rawOutput.equals(that.rawOutput);
    }

    @Override
    public int hashCode() {
        int result = dateTimeOnServer.hashCode();
        result = 31 * result + totalOverallBackupJobs;
        result = 31 * result + totalCompletedStatus;
        result = 31 * result + totalNotCompletedStatus;
        result = 31 * result + rawOutput.hashCode();
        return result;
    }
}
