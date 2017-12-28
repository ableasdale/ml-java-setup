package com.marklogic.support.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class XQueryBuilder {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String XQUERY_10ML_DECL = "xquery version \"1.0-ml\";\n\n";
    public static final String IMPORT_ADMIN = "import module namespace admin = \"http://marklogic.com/xdmp/admin\" at \"/MarkLogic/admin.xqy\";\n";

    public static final String GET_CONFIG = "let $config := admin:get-configuration()\n";
    public static final String SAVE_CONFIG = "return admin:save-configuration($config);\n";

    public static String databaseCreate(String database) {
        return String.format("let $config := admin:database-create($config, \"%s\", xdmp:database(\"Security\"), xdmp:database(\"Schemas\"))\n", database);
    }

    public static String forestCreate(String forestname, String hostname, String dataDirectory) {
        return String.format("let $config := admin:forest-create($config, \"%s\", xdmp:host(\"%s\"), \"%s\")\n", forestname, hostname, dataDirectory);
    }

    public static String dbAttachForest(String database, String forestname) {
        return String.format("let $config := admin:database-attach-forest($config, xdmp:database(\"%s\"), xdmp:forest(\"%s\"))\n", database, forestname);
    }

    public static String forestAddReplica(String forestname, String replicaforestname) {
        return String.format("let $config := admin:forest-add-replica($config, xdmp:forest(\"%s\"), xdmp:forest(\"%s\"))\n", forestname, replicaforestname);
    }

    public static String setGroupFileLogging(String loglevel){
        return "let $config := admin:group-set-file-log-level($config, admin:group-get-id($config, \"Default\"), \""+loglevel+"\")";
    }

    public static String configureBaseGroupSettings(){
        StringBuilder sb = new StringBuilder();
        sb.append(XQUERY_10ML_DECL).append(IMPORT_ADMIN);
        sb.append(GET_CONFIG);
        sb.append(setGroupFileLogging("debug"));
        sb.append(SAVE_CONFIG);
        return prepareEncodedXQuery(sb);
    }

    public static String createDatabaseAndForests(String[] hosts, String[] databases, String dataDirectory, int forestsperhost) {
        int forestCount = 1;
        List<String> hostList = Arrays.asList(hosts);

        StringBuilder sb = new StringBuilder();
        sb.append(XQUERY_10ML_DECL).append(IMPORT_ADMIN);

        // 1. Create database[s]
        sb.append(GET_CONFIG);
        for (String db : databases){
            sb.append(databaseCreate(db));
        }
        sb.append(SAVE_CONFIG);

        // 2. Create master forest[s]
        sb.append(IMPORT_ADMIN).append(GET_CONFIG);
        for (String db : databases){
            forestCount = 1;
            for (String h : hosts) {
                for (int i=0; i<forestsperhost; i++){
                    sb.append(forestCreate(String.format("%s-%d", db, forestCount), h, dataDirectory ));
                    forestCount++;
                }
            }
        }

        // 3. Create replica forest[s]
        forestCount = 1;
        Collections.rotate(hostList, 1);

        for (String db : databases){
            forestCount = 1;
            for (String r : hostList) {
                for (int i=0; i<forestsperhost; i++){
                    sb.append(forestCreate(String.format("%s-%d-R", db, forestCount), r, dataDirectory ));
                    forestCount++;
                }
            }
        }
        sb.append(SAVE_CONFIG);

        // 4. Attach master forest[s]
        sb.append(IMPORT_ADMIN).append(GET_CONFIG);
        for (String db : databases){
            for(int i=1; i<forestCount; i++){
                sb.append(dbAttachForest(db, String.format("%s-%d", db, i)));
            }
        }

        // 5. Attach replica forest[s]
        for (String db : databases){
            for(int i=1; i<forestCount; i++){
                sb.append(forestAddReplica(String.format("%s-%d", db, i), String.format("%s-%d-R", db, i)));
            }
        }

        sb.append(SAVE_CONFIG);
        return prepareEncodedXQuery(sb);
    }

    private static String prepareEncodedXQuery(StringBuilder sb) {
        try {
            LOG.debug(URLEncoder.encode(sb.toString(), "UTF-8"));
            return String.format("xquery=%s", URLEncoder.encode(sb.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error("UnsupportedEncodingException: ",e);
            return sb.toString();
        }
    }

    public static String createSampleDocData(String database){
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/main/resources/test-data.xqy")));
            return String.format("database=%s&xquery=%s", database, URLEncoder.encode(content, "UTF-8"));
        } catch (IOException e) {
            LOG.error("IOException: ",e);
        }
        return "Request Failed";
    };
}
