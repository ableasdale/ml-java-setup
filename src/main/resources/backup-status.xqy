xquery version "1.0-ml";

declare namespace job = "http://marklogic.com/xdmp/job-status";
declare variable $TOTAL-JOBS := xdmp:database-backup-status((), xdmp:hosts());

<backupStats>
    <dateTimeOnServer>{fn:current-dateTime()}</dateTimeOnServer>
    <totalOverallBackupJobs>{count($TOTAL-JOBS)}</totalOverallBackupJobs>
    <totalCompletedStatus>{count($TOTAL-JOBS//job:status[. eq "completed"])}</totalCompletedStatus>
    <totalNotCompletedStatus>{count($TOTAL-JOBS//job:status[. ne "completed"])}</totalNotCompletedStatus>
    <rawOutput>{string-join(
            for $i in $TOTAL-JOBS return (
                xs:string($i/job:job-id),
                xs:string($i/job:status),
                for $j in $i/job:forest return fn:concat(xs:string($j/job:forest-name),"~", xs:string($j/job:status),"~",xs:string($j/job:backup-path),"~",xs:string($j/job:incremental-backup-path))),", ")}</rawOutput>
</backupStats>
