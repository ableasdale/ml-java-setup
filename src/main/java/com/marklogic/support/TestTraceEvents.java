package com.marklogic.support;

import com.marklogic.support.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class TestTraceEvents {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        String[] traceevents = Util.getConfiguration().getStringArray("traceevents");
        for (String s : traceevents){
            LOG.info(s);
        }
    }
}
