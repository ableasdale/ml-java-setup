package com.marklogic.support.handlers;

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
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        Map m = Statistics.getStatisticsMap();
        if (m.size() > 0) {
            m.forEach((k, v) -> {
                StatsTracker s = (StatsTracker) v;
                sb.append("['").append(Util.extractTimeFromDateTime(s.getDateTimeOnServer())).append("',").append(s.getTotalUnclosedStands()).append("],");
                sb2.append("['").append(Util.extractTimeFromDateTime(s.getDateTimeOnServer())).append("',").append(s.getTotalDocs()).append("],");
            });
        } else {
            sb.append("['No Data...',0]"); // waiting for data
            sb2.append("['No Data...',0]"); // waiting for data
        }

        try {
            String page = new String(Files.readAllBytes(Paths.get("src/main/resources/google-charts-html")));
            String page2 = page.replace("%1%", sb.toString());
            return page2.replace("%2%", sb2.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Error";
    }
}