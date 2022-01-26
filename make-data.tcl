#!/usr/bin/env tclsh

package require uuid
package require json::write

set testgroup "test-group-1"

set json_file [ open data.json w ]
set csv_file [ open data.csv w ]

puts $json_file "\{\"items\": \[\n"

set start 1

for { set i [ lindex $argv 0 ] } { $i > 0 } { incr i -1 } {
    set id [ uuid::uuid generate ]
    puts $csv_file $id
    if { !$start } {
        puts $json_file ","
    } else {
        set start 0
    }
    puts $json_file [ json::write object op [ json::write string upsert ] item_key [ json::write string $id ] item_value [ json::write string $testgroup ] ]
}

puts $json_file "] }"

close $json_file
close $csv_file
