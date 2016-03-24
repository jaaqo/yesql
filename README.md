# Jeesql - a fork of yesql with an upgrade path

Jeesql is a Clojure library for _using_ SQL. Please see krisajenkins/yesql for yesql documentation.
I will only document the notable differences here.

## Notable differences to yesql 0.5 branch

* Connection is *always* first parameter to query function
* Connection cannot be given in defqueries phase
* Generate positional arguments when given :positional? true argument (for upgrade path from yesql <= 0.4 versions)
 

## Installation

Add this to your [Leiningen](https://github.com/technomancy/leiningen) `:dependencies`:

[![Clojars Project](http://clojars.org/jeesql/latest-version.svg)](http://clojars.org/jeesql)
