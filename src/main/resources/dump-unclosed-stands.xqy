xquery version "1.0-ml";

declare namespace f = "http://marklogic.com/xdmp/status/forest";

declare variable $unclosed := xdmp:forest-status(xdmp:database-forests(xdmp:database("PrimaryApplication")))//f:unclosed-stand;
text {for $i in $unclosed order by $i/f:reference/f:earliest
return ("Id:",$i/f:stand-id,"Path:",$i/f:path, "Earliest:",$i/f:reference/f:earliest, "Latest:",$i/f:reference/f:latest,'&#10;'),count($unclosed)}