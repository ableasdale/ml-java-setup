package com.marklogic.support.handlers;

import com.marklogic.support.beans.BackupStats;
import com.marklogic.support.beans.StatsTracker;
import com.marklogic.support.providers.Statistics;
import com.marklogic.support.util.Util;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Dashboard implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
        exchange.getResponseSender().send(createChartView());
    }

    private String createChartView() {
        StringBuilder unclosedStands = new StringBuilder();
        StringBuilder documentStats = new StringBuilder();
        StringBuilder backUpStats = new StringBuilder();
        StringBuilder tripleCount = new StringBuilder();

        Map m1 = Statistics.getBackupStatisticsMap();
        if (m1.size() > 0) {
            m1.forEach((k,v) -> {
                BackupStats s = (BackupStats) v;
                backUpStats.append("['").append(Util.extractTimeFromDateTime(s.getDateTimeOnServer())).append("',").append(s.getTotalOverallBackupJobs()).append(",").append(s.getTotalCompletedStatus()).append(",").append(s.getTotalNotCompletedStatus()).append("],");
            });

        } else {
            backUpStats.append("['No Data...',0,0,0]");
        }


        Map m = Statistics.getStatisticsMap();
        if (m.size() > 0) {
            m.forEach((k, v) -> {
                StatsTracker s = (StatsTracker) v;
                unclosedStands.append("['").append(Util.extractTimeFromDateTime(s.getDateTimeOnServer())).append("',").append(s.getTotalUnclosedStands()).append("],");
                documentStats.append("['").append(Util.extractTimeFromDateTime(s.getDateTimeOnServer())).append("',").append(s.getTotalDocs()).append(",").append(s.getTotalUniqueSubjects()).append(",").append(s.getTotalUniquePredicates()).append("],");
                tripleCount.append("['").append(Util.extractTimeFromDateTime(s.getDateTimeOnServer())).append("',").append(s.getTotalTriples()).append("],");
            });
        } else {
            unclosedStands.append("['No Data...',0]"); // waiting for data
            documentStats.append("['No Data...',0,0,0]"); // waiting for data
            tripleCount.append("['No Data...',0]");
        }

        try {
            String page = new String(Files.readAllBytes(Paths.get("src/main/resources/google-charts-html")));
            String page2 = page.replace("%1%", unclosedStands.toString());
            String page3 = page2.replace("%2%", documentStats.toString());
            String page4 = page3.replace("%3%", backUpStats.toString());
            return page4.replace("%4%", tripleCount.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Error";
    }
}