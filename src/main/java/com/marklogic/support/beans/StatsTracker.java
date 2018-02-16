package com.marklogic.support.beans;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StatsTracker {

    private String dateTimeOnServer;
    private long totalDocs;
    private long totalTriples;
    private long totalUniqueSubjects;
    private long totalUniquePredicates;
    private long totalUniqueObjects;
    private int totalUnclosedStands;
    private String earliestDateTime;
    private String earliestDateTimeRange;
    private String idOfEarliestStand;
    private String pathOfEarliestStand;
    private String rawOutput;

    public String getDateTimeOnServer() {
        return dateTimeOnServer;
    }

    public void setDateTimeOnServer(String dateTimeOnServer) {
        this.dateTimeOnServer = dateTimeOnServer;
    }

    public long getTotalDocs() {
        return totalDocs;
    }

    public void setTotalDocs(long totalDocs) {
        this.totalDocs = totalDocs;
    }

    public long getTotalTriples() {
        return totalTriples;
    }

    public void setTotalTriples(long totalTriples) {
        this.totalTriples = totalTriples;
    }

    public long getTotalUniqueSubjects() {
        return totalUniqueSubjects;
    }

    public void setTotalUniqueSubjects(long totalUniqueSubjects) {
        this.totalUniqueSubjects = totalUniqueSubjects;
    }

    public long getTotalUniquePredicates() {
        return totalUniquePredicates;
    }

    public void setTotalUniquePredicates(long totalUniquePredicates) {
        this.totalUniquePredicates = totalUniquePredicates;
    }

    public long getTotalUniqueObjects() {
        return totalUniqueObjects;
    }

    public void setTotalUniqueObjects(long totalUniqueObjects) {
        this.totalUniqueObjects = totalUniqueObjects;
    }

    public int getTotalUnclosedStands() {
        return totalUnclosedStands;
    }

    public void setTotalUnclosedStands(int totalUnclosedStands) {
        this.totalUnclosedStands = totalUnclosedStands;
    }

    public String getEarliestDateTime() {
        return earliestDateTime;
    }

    public void setEarliestDateTime(String earliestDateTime) {
        this.earliestDateTime = earliestDateTime;
    }

    public String getEarliestDateTimeRange() {
        return earliestDateTimeRange;
    }

    public void setEarliestDateTimeRange(String earliestDateTimeRange) {
        this.earliestDateTimeRange = earliestDateTimeRange;
    }

    public String getIdOfEarliestStand() {
        return idOfEarliestStand;
    }

    public void setIdOfEarliestStand(String idOfEarliestStand) {
        this.idOfEarliestStand = idOfEarliestStand;
    }

    public String getPathOfEarliestStand() {
        return pathOfEarliestStand;
    }

    public void setPathOfEarliestStand(String pathOfEarliestStand) {
        this.pathOfEarliestStand = pathOfEarliestStand;
    }

    public String getRawOutput() {
        return rawOutput;
    }

    public void setRawOutput(String rawOutput) {
        this.rawOutput = rawOutput;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatsTracker that = (StatsTracker) o;

        if (totalDocs != that.totalDocs) return false;
        if (totalTriples != that.totalTriples) return false;
        if (totalUniqueSubjects != that.totalUniqueSubjects) return false;
        if (totalUniquePredicates != that.totalUniquePredicates) return false;
        if (totalUniqueObjects != that.totalUniqueObjects) return false;
        if (totalUnclosedStands != that.totalUnclosedStands) return false;
        if (!dateTimeOnServer.equals(that.dateTimeOnServer)) return false;
        if (!earliestDateTime.equals(that.earliestDateTime)) return false;
        if (!earliestDateTimeRange.equals(that.earliestDateTimeRange)) return false;
        if (!idOfEarliestStand.equals(that.idOfEarliestStand)) return false;
        if (!pathOfEarliestStand.equals(that.pathOfEarliestStand)) return false;
        return rawOutput.equals(that.rawOutput);
    }

    @Override
    public int hashCode() {
        int result = dateTimeOnServer.hashCode();
        result = 31 * result + (int) (totalDocs ^ (totalDocs >>> 32));
        result = 31 * result + (int) (totalTriples ^ (totalTriples >>> 32));
        result = 31 * result + (int) (totalUniqueSubjects ^ (totalUniqueSubjects >>> 32));
        result = 31 * result + (int) (totalUniquePredicates ^ (totalUniquePredicates >>> 32));
        result = 31 * result + (int) (totalUniqueObjects ^ (totalUniqueObjects >>> 32));
        result = 31 * result + totalUnclosedStands;
        result = 31 * result + earliestDateTime.hashCode();
        result = 31 * result + earliestDateTimeRange.hashCode();
        result = 31 * result + idOfEarliestStand.hashCode();
        result = 31 * result + pathOfEarliestStand.hashCode();
        result = 31 * result + rawOutput.hashCode();
        return result;
    }
}
