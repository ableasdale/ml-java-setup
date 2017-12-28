package com.marklogic.support.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
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
        return "let $config := admin:database-create($config, \""+database+"\", xdmp:database(\"Security\"), xdmp:database(\"Schemas\"))\n";
    }

    public static String forestCreate(String forestname, String hostname, String dataDirectory) {
        return "let $config := admin:forest-create($config, \""+forestname+"\", xdmp:host(\""+hostname+"\"), \""+dataDirectory+"\")\n";
    }

    public static String dbAttachForest(String database, String forestname) {
        return "let $config := admin:database-attach-forest($config, xdmp:database(\""+database+"\"), xdmp:forest(\""+forestname+"\"))\n";
    }

    public static String forestAddReplica(String forestname, String replicaforestname) {
        return "let $config := admin:forest-add-replica($config, xdmp:forest(\""+forestname+"\"), xdmp:forest(\""+replicaforestname+"\"))\n";
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
                    sb.append(forestCreate(db+"-"+forestCount, h, dataDirectory ));
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
                    sb.append(forestCreate(db+"-"+forestCount+"-R", r, dataDirectory ));
                    forestCount++;
                }
            }
        }
        sb.append(SAVE_CONFIG);

        // 4. Attach master forest[s]
        sb.append(IMPORT_ADMIN).append(GET_CONFIG);
        for (String db : databases){
            for(int i=1; i<forestCount; i++){
                sb.append(dbAttachForest(db,  db+"-"+i));
            }
        }

        // 5. Attach replica forest[s]
        for (String db : databases){
            for(int i=1; i<forestCount; i++){
                sb.append(forestAddReplica(db+"-"+i,  db+"-"+i+"-R"));
            }
        }

        /*
        for (int i=1; i<hosts.length; i++) {
            for (int i=1; i<)
            sb.append(forestCreate(db+"-"+forestCount, h, dataDirectory ));
        }h*/

        sb.append(SAVE_CONFIG);

        LOG.info(sb.toString());

        return sb.toString();
    }

}
