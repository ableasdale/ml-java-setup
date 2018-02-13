xquery version "1.0-ml";

import module namespace sem = "http://marklogic.com/semantics" at "MarkLogic/semantics.xqy";

declare variable $BATCH := xs:string(xdmp:random());
declare variable $GROUP := xs:string(xdmp:random());

sem:rdf-insert(for $i in 1 to 1000 return sem:triple(sem:iri($BATCH), sem:iri($GROUP), xdmp:random()))
