package com.marklogic.support.handlers;

import com.marklogic.support.beans.StatsTracker;
import com.marklogic.support.providers.Statistics;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.Map;

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

    public String getXmlAsString(StatsTracker s) {
        Marshaller jaxbMarshaller = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(StatsTracker.class);
            jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(s, sw);
            return sw.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return "NULL";
    }

    public String everythingInMap() {
        Map m = Statistics.getStatisticsMap();
        StringBuilder sb = new StringBuilder();
        m.forEach( (k, v) -> sb.append("Key: " + k + "\nValue: " + getXmlAsString((StatsTracker) v) +"\n"));
        return sb.toString();
    }

}