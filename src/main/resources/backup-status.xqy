xquery version "1.0-ml";

declare namespace job = "http://marklogic.com/xdmp/job-status";
declare variable $TOTAL-JOBS := xdmp:database-backup-status((), xdmp:hosts());

count($TOTAL-JOBS),
count($TOTAL-JOBS//job:status[. eq "completed"]),
count($TOTAL-JOBS//job:status[. ne "completed"])