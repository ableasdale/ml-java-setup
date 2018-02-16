package com.marklogic.support.handlers;

import com.marklogic.support.beans.StatsTracker;
import com.marklogic.support.providers.Statistics;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.util.Map;
import java.util.Map.Entry;

public class Data implements HttpHandler {

    private final String value;
    public Data(String value) {
        this.value = value;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send(everythingInMap());
    }

    public String everythingInMap() {

        Map m = Statistics.getStatisticsMap();
        StringBuilder sb = new StringBuilder();


        m.forEach( (k,v) -> sb.append("Key: " + k + " : Value: " + v +"\n"));

//        for (Entry<?,?> entry : m.entrySet()) {
//            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//        }
//
//
//        for ((String)k : m.keySet()) {
//            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//        }
//        LOG.info("\n\nMap Size: " + Statistics.getStatisticsMap().size());
        return sb.toString();
    }

}