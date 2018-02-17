package com.marklogic.support.providers;

import com.marklogic.support.beans.BackupStats;
import com.marklogic.support.beans.StatsTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Map;

public class Statistics {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static Map<String, StatsTracker> statsTrackerMap;
    private static Map<String, BackupStats> backupStatsMap;

    private Statistics() {
        statsTrackerMap = new LinkedHashMap<>();
        backupStatsMap = new LinkedHashMap<>();
    }

    private static class LazyHolder {
        static final Statistics INSTANCE = new Statistics();
    }

    public static Statistics getInstance() {
        return LazyHolder.INSTANCE;
    }

    public static Map getStatisticsMap() {
        return getInstance().statsTrackerMap;
    }

    public static Map getBackupStatisticsMap() {
        return getInstance().backupStatsMap;
    }
}
