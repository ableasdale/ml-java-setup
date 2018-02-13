package com.marklogic.support.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;

public class XQueryDataBuilder {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static String azAZCharactersDecl() {
        return "declare variable $STRING := string-join((for $i in 1 to 26 return codepoints-to-string(64 + $i), for $j in 1 to 26 return codepoints-to-string(96 + $j)), \"\");\n";
    }

    public static String randomAlphaString() {
        return new StringBuilder().append("declare function local:random-alpha-string($length as xs:integer)  {\n").append("  string-join(for $i in 1 to $length\n").append("  return fn:substring($STRING, (xdmp:random(51) + 1), 1))\n").append("};\n").toString();
    }

    public static String updateAutoCommitEvalOpts() {
        return "<options xmlns=\"xdmp:eval\"><transaction-mode>update-auto-commit</transaction-mode></options>)";
    }

    public static String constructInnerDocumentInsertLoop(String[] indexes) {
        StringBuilder sb = new StringBuilder();
        sb.append("for $i at $pos in 1 to 1000\n");
        sb.append("return xdmp:document-insert(\n");
        sb.append("fn:concat(\"/\", xdmp:random(), \".xml\"),\n");
        sb.append("element test-data {\n");

        Iterator<String> stringIterator = Arrays.asList(indexes).iterator();
        while(stringIterator.hasNext()) {
            sb.append(String.format("element %s{fn:string-join(for $i in 1 to xdmp:random(25) return local:random-alpha-string(35),\" \")}", stringIterator.next()));
            if(stringIterator.hasNext()) {
                sb.append(",\n");
            }
        }

        sb.append("}\n");
        sb.append(")\n");
        return sb.toString();
    }

    public static String createComplexSampleDocData(String[] indexes) {
        StringBuilder sb = new StringBuilder();
        sb.append(XQueryBuilder.XQUERY_10ML_DECL);
        sb.append(azAZCharactersDecl()).append(randomAlphaString());
        sb.append("for $x in (1 to 10)\n");
        sb.append("return xdmp:spawn-function(function() {\n");
        sb.append(constructInnerDocumentInsertLoop(indexes));
        sb.append("},\n");
        sb.append(updateAutoCommitEvalOpts());
        return sb.toString();
        /*
        return "xquery version \"1.0-ml\";\n" +
                "\n" +
                "for $x in (1 to 1000)\n" +
                "return xdmp:spawn-function(function() {\n" +
                "    for $i at $pos in 1 to 10\n" +
                "        return xdmp:document-insert(\n" +
                "            fn:concat(\"/\", xdmp:random(), \".xml\"),\n" +
                "            element test-data {\n" +
                "                element id {$pos},\n" +
                "                element dateTime {fn:current-dateTime()},\n" +
                "                element data {xdmp:random()}\n" +
                "            }\n" +
                "        )\n" +
                "    },\n" +
                "    <options xmlns=\"xdmp:eval\"><transaction-mode>update-auto-commit</transaction-mode></options>)";
    */
    }

    public static String createSampleDocData(String database, String[] indexes) {
        try {
            return String.format("database=%s&xquery=%s", database, URLEncoder.encode(createComplexSampleDocData(indexes), "UTF-8"));
        } catch (IOException e) {
            LOG.error("IOException: ", e);
        }
        return "Request Failed";
    }
}
