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
import java.util.Iterator;
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

    public static String setGroupFileLogging(String loglevel) {
        return String.format("let $config := admin:group-set-file-log-level($config, admin:group-get-id($config, \"Default\"), \"%s\")\n", loglevel);
    }

    public static String setBackgroundIoLimit(int value) {
        return String.format("let $config := admin:group-set-background-io-limit($config, admin:group-get-id($config, \"Default\"), %d)\n", value);
    }

    /* admin:database-set-two-character-searches(
$config as element(configuration),
$database-id as xs:unsignedLong,
$value as xs:boolean
) as element(configuration)
*/
    public static String databaseSetTrue(String item, String database) {
        return String.format("let $config := admin:database-set-%s( $config, xdmp:database(\"%s\"), fn:true() )\n", item, database);
    }

    /*
    public static String databaseConfigureStringRangeIndex(String item, String database) {
        return "let $config := admin:database-set-"+item+"( $config, xdmp:database(\""+database+"\"), fn:true() )\n";
    }*/

    public static String scheduleMinutelyBackup(String database, String backupDirectory, int interval, int numberOfBackupsToRetain) {
        return String.format("let $config := admin:database-add-backup($config, xdmp:database(\"%s\"), admin:database-minutely-backup(\"%s\", %d, %d, true(), true(), true(), false()))\n", database, backupDirectory, interval, numberOfBackupsToRetain);
    }

    public static String scheduleMinutelyIncrementalBackup(String database, String backupDirectory, int interval) {
        return String.format("let $config := admin:database-add-backup($config, xdmp:database(\"%s\"), admin:database-minutely-incremental-backup(\"%s\", %d, true(), true(), true(), false()))\n", database, backupDirectory, interval);
    }

    public static String scheduleHourlyBackup(String database, String backupDirectory, int interval, int numberOfBackupsToRetain) {
        return String.format("let $config := admin:database-add-backup($config, xdmp:database(\"%s\"), admin:database-hourly-backup(\"%s\", %d, %d, true(), true(), true(), false()))\n", database, backupDirectory, interval, numberOfBackupsToRetain);
    }

    public static String getForestStatusForDatabase(String database){
       return prepareEncodedXQuery(String.format("xdmp:forest-status(xdmp:database-forests(xdmp:database(\"%s\")))", database));
    }

    public static String configureDatabaseIndexes(String[] databases, String[] indexes) {
        StringBuilder sb = new StringBuilder();
        sb.append(XQUERY_10ML_DECL).append(IMPORT_ADMIN).append(GET_CONFIG);
        for (String db : databases) {
            for (String idx : indexes){
                sb.append(databaseSetTrue(idx, db));
            }
        }
        sb.append(SAVE_CONFIG);
        return prepareEncodedXQuery(sb);
    }

    public static String configureDatabaseStringRangeIndexes(String[] databases, String[] indexes) {
        StringBuilder sb = new StringBuilder();
        sb.append(XQUERY_10ML_DECL).append(IMPORT_ADMIN).append(GET_CONFIG);
        for (String db : databases) {
            sb.append(String.format("let $rangespec-%s := (\n", db));
            Iterator<String> stringIterator = Arrays.asList(indexes).iterator();
            while(stringIterator.hasNext()) {
                sb.append(String.format("admin:database-range-element-index(\"string\", (), \"%s\", \"http://marklogic.com/collation/codepoint\", fn:false() )", stringIterator.next()));
                if(stringIterator.hasNext()) {
                    sb.append(",\n");
                }
            }
            sb.append(")\n");
            sb.append(String.format("let $config := admin:database-add-range-element-index($config, xdmp:database(\"%s\"), $rangespec-%s)\n", db, db));
        }
        sb.append(SAVE_CONFIG);
        return prepareEncodedXQuery(sb);
    }

    public static String configureTraceEvents(String[] traceEvents) {
        StringBuilder sb = new StringBuilder();
        sb.append(XQUERY_10ML_DECL).append(IMPORT_ADMIN).append(GET_CONFIG);
        sb.append("let $config := admin:group-set-trace-events-activated($config, admin:group-get-id($config, \"Default\"), fn:true())\n");
        sb.append("let $config := admin:group-add-trace-event($config, admin:group-get-id($config, \"Default\"),(\n");

        Iterator<String> stringIterator = Arrays.asList(traceEvents).iterator();
        while(stringIterator.hasNext()) {
            sb.append(String.format("admin:group-trace-event(\"%s\")", stringIterator.next()));
            if(stringIterator.hasNext()) {
                sb.append(",\n");
            }
        }

        sb.append("))\n").append(SAVE_CONFIG);
        return prepareEncodedXQuery(sb);
    }

    public static String configureBaseGroupSettings(int backgroundIoLimit) {
        StringBuilder sb = new StringBuilder();
        sb.append(XQUERY_10ML_DECL).append(IMPORT_ADMIN).append(GET_CONFIG);
        sb.append(setGroupFileLogging("debug"));
        sb.append(setBackgroundIoLimit(backgroundIoLimit));
        sb.append(SAVE_CONFIG);
        return prepareEncodedXQuery(sb);
    }

    public static String configureScheduledMinutelyBackups(String[] databases, String backupDirectory, int interval, int numberOfBackupsToRetain) {
        StringBuilder sb = new StringBuilder();
        sb.append(XQUERY_10ML_DECL).append(IMPORT_ADMIN).append(GET_CONFIG);
        for (String db : databases) {
            sb.append(scheduleMinutelyBackup(db, backupDirectory, interval, numberOfBackupsToRetain));
        }
        sb.append(SAVE_CONFIG);
        return prepareEncodedXQuery(sb);
    }

    // TODO - refactor later
    public static String configureScheduledMinutelyIncrementalBackups(String[] databases, String backupDirectory, int interval) {
        StringBuilder sb = new StringBuilder();
        sb.append(XQUERY_10ML_DECL).append(IMPORT_ADMIN).append(GET_CONFIG);
        for (String db : databases) {
            sb.append(scheduleMinutelyIncrementalBackup(db, backupDirectory, interval));
        }
        sb.append(SAVE_CONFIG);
        return prepareEncodedXQuery(sb);
    }

    public static String createInitialBackup(String[] databases, String backupDirectory) {
        StringBuilder sb = new StringBuilder();
        sb.append(XQUERY_10ML_DECL);
        for (String db : databases) {
            sb.append(String.format("xdmp:database-backup(xdmp:database-forests(xdmp:database(\"%s\")), \"%s\")\n", db, backupDirectory));
        /* xdmp:database-backup(
        $forestIDs as xs:unsignedLong*,
                $pathname as xs:string,
   [$journal-archiving as xs:boolean?],
   [$journal-archive-path as xs:string?],
   [$lag-limit as xs:unsignedLong?],
   [$backup-kek-id as xs:string?],
   [$backup-passphrase as xs:string?]
) as xs:unsignedLong */
        }
        LOG.info(sb.toString());
        return prepareEncodedXQuery(sb);
    }


    public static String createDatabaseAndForests(String[] hosts, String[] databases, String dataDirectory, int forestsperhost) {
        int forestCount = 1;
        List<String> hostList = Arrays.asList(hosts);

        StringBuilder sb = new StringBuilder();
        sb.append(XQUERY_10ML_DECL).append(IMPORT_ADMIN);

        // 1. Create database[s]
        sb.append(GET_CONFIG);
        for (String db : databases) {
            sb.append(databaseCreate(db));
        }
        sb.append(SAVE_CONFIG);

        // 2. Create master forest[s]
        sb.append(IMPORT_ADMIN).append(GET_CONFIG);
        for (String db : databases) {
            forestCount = 1;
            for (String h : hosts) {
                for (int i = 0; i < forestsperhost; i++) {
                    sb.append(forestCreate(String.format("%s-%d", db, forestCount), h, dataDirectory));
                    forestCount++;
                }
            }
        }

        // 3. Create replica forest[s]
        forestCount = 1;
        Collections.rotate(hostList, 1);

        for (String db : databases) {
            forestCount = 1;
            for (String r : hostList) {
                for (int i = 0; i < forestsperhost; i++) {
                    sb.append(forestCreate(String.format("%s-%d-R", db, forestCount), r, dataDirectory));
                    forestCount++;
                }
            }
        }
        sb.append(SAVE_CONFIG);

        // 4. Attach master forest[s]
        sb.append(IMPORT_ADMIN).append(GET_CONFIG);
        for (String db : databases) {
            for (int i = 1; i < forestCount; i++) {
                sb.append(dbAttachForest(db, String.format("%s-%d", db, i)));
            }
        }

        // 5. Attach replica forest[s]
        for (String db : databases) {
            for (int i = 1; i < forestCount; i++) {
                sb.append(forestAddReplica(String.format("%s-%d", db, i), String.format("%s-%d-R", db, i)));
            }
        }

        sb.append(SAVE_CONFIG);
        return prepareEncodedXQuery(sb);
    }

    private static String prepareEncodedXQuery(String s) {
        try {
            LOG.debug(URLEncoder.encode(s, "UTF-8"));
            return String.format("xquery=%s", URLEncoder.encode(s, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error("UnsupportedEncodingException: ", e);
            return s;
        }
    }

    private static String prepareEncodedXQuery(StringBuilder sb) {
        return prepareEncodedXQuery(sb.toString());
    }

    public static String evaluateXQueryModuleAgainstDatabase(String database, String filename) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filename)));
            return String.format("database=%s&xquery=%s", database, URLEncoder.encode(content, "UTF-8"));
        } catch (IOException e) {
            LOG.error("IOException: ", e);
        }
        return "Request Failed";
    }

    public static void loadTripleDataIntoMarkLogic(String[] hosts, String[] databases) {
        for (String h : hosts) {
            for (String d : databases) {
                Util.processHttpRequest(Requests.evaluateXQuery(h, evaluateXQueryModuleAgainstDatabase(d, "src/main/resources/create-random-triples.xqy") ));
            }
        }
    }


}
