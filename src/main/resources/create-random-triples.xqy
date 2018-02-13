xquery version "1.0-ml";

import module namespace sem = "http://marklogic.com/semantics" at "MarkLogic/semantics.xqy";

declare variable $BATCH := xdmp:random();

declare function local:create-triple() {
    sem:triple(sem:iri("subject-"||$BATCH), sem:iri("predicate"||xdmp:random()), "object"||xdmp:random())
};

sem:rdf-insert(for $i in 1 to 1000 return local:create-triple())