xquery version "1.0-ml";

declare namespace f = "http://marklogic.com/xdmp/status/forest";

for $i in xdmp:forest-status(xdmp:database-forests(xdmp:database("PrimaryApplication")))//f:unclosed-stand
return text {"Id:",$i/f:stand-id,"Path:",$i/f:path, "Earliest:",$i/f:reference/f:earliest, "Latest:",$i/f:reference/f:latest}