xquery version "1.0-ml";

for $x in (1 to 500)
return xdmp:spawn-function(function() {
    for $i at $pos in 1 to 10
        return xdmp:document-insert(
            fn:concat("/", xdmp:random(), ".xml"),
            element test-data {
                element id {$pos},
                element dateTime {fn:current-dateTime()},
                element data {xdmp:random()}
            }
        )
    },
    <options xmlns="xdmp:eval">
        <transaction-mode>update-auto-commit</transaction-mode>
    </options>)