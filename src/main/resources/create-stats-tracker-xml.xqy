xquery version "1.0-ml";

declare namespace f = "http://marklogic.com/xdmp/status/forest";

import module  namespace sem = "http://marklogic.com/semantics" at "MarkLogic/semantics.xqy";

declare variable $current-dateTime := fn:current-dateTime();
declare variable $unclosed := xdmp:forest-status(xdmp:database-forests(xdmp:database("PrimaryApplication")))//f:unclosed-stand;
declare variable $triple-stats := cts:triple-value-statistics();
declare variable $earliest-unclosed-dateTime := fn:min($unclosed/f:reference/f:earliest);
declare variable $earliest-unclosed := $unclosed[f:reference/f:earliest eq $earliest-unclosed-dateTime][1];

<statsTracker>
    <dateTimeOnServer>{$current-dateTime}</dateTimeOnServer>
    <earliestDateTime>{$earliest-unclosed-dateTime}</earliestDateTime>
    <ageOfEarliestDateTime>{xs:duration($current-dateTime - $earliest-unclosed-dateTime)}</ageOfEarliestDateTime>
    <earliestDateTimeRange>{xs:duration(fn:max($unclosed/f:reference/f:earliest) - fn:min($unclosed/f:reference/f:earliest))}</earliestDateTimeRange>
    <latestDateTimeRange>{xs:duration(fn:max($unclosed/f:reference/f:latest) - fn:min($unclosed/f:reference/f:latest))}</latestDateTimeRange>
    <idOfEarliestStand>{fn:data($earliest-unclosed/f:stand-id)}</idOfEarliestStand>
    <pathOfEarliestStand>{fn:data($earliest-unclosed/f:path)}</pathOfEarliestStand>
    <totalDocs>{xdmp:estimate(doc())}</totalDocs>
    <totalTriples>{fn:data($triple-stats/@count)}</totalTriples>
    <totalUnclosedStands>{fn:count($unclosed)}</totalUnclosedStands>
    <totalUniqueObjects>{fn:data($triple-stats/@unique-objects)}</totalUniqueObjects>
    <totalUniquePredicates>{fn:data($triple-stats/@unique-predicates)}</totalUniquePredicates>
    <totalUniqueSubjects>{fn:data($triple-stats/@unique-subjects)}</totalUniqueSubjects>
    <rawOutput>{text {for $i in $unclosed order by $i/f:reference/f:earliest
    return ("Id:",$i/f:stand-id,"Path:",$i/f:path, "Earliest:",$i/f:reference/f:earliest, "Latest:",$i/f:reference/f:latest,'&#10;')}}</rawOutput>
</statsTracker>