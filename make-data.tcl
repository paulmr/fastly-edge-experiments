#!/usr/bin/env tclsh

package require uuid

for { set i [ lindex $argv 0 ] } { $i > 0 } { incr i -1 } {
    puts [ uuid::uuid generate ]
}
