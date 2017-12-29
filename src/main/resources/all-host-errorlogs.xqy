xquery version "1.0-ml";

declare variable $HOST-ERRORLOGS as xs:string* := for $i in xdmp:hosts() return "file://"||xdmp:host-name($i)||xdmp:data-directory()||"/Logs/ErrorLog.txt";

let $zip := xdmp:zip-create(
        <parts xmlns="xdmp:zip">{for $i in xdmp:hosts() return element part {xdmp:host-name($i)||"-ErrorLog.txt"}}</parts>,
        (for $i in $HOST-ERRORLOGS return xdmp:document-get($i))
)
return
    xdmp:save("/tmp/ErrorLogs-"||fn:format-dateTime(fn:current-dateTime(),"[Y01]_[M01]_[D01]_[H01]_[m01]_[s01]")||".zip", $zip,
        <options xmlns="xdmp:save">
            <encoding>utf8</encoding>
        </options>)