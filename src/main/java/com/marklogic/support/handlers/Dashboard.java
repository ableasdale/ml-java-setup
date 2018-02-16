package com.marklogic.support.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Dashboard implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
        exchange.getResponseSender().send(new String(Files.readAllBytes(Paths.get("src/main/resources/google-charts-html"))));
    }
}
/*
    .setHandler(exchange -> {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
                    exchange.getResponseSender().send(new String(Files.readAllBytes(Paths.get("src/main/resources/google-charts-html"))));
                })
 */