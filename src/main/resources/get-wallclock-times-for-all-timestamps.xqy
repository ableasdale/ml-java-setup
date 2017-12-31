xquery version "1.0-ml;

element forests {
    xdmp:forest-status(xdmp:database-forests(xdmp:database("PrimaryApplication")))//*:timestamp-table//*:timestamp ! xdmp:timestamp-to-wallclock(.)
}