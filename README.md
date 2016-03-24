# Jeesql - a fork of yesql with an upgrade path

Jeesql is a Clojure library for _using_ SQL. Please see krisajenkins/yesql for yesql documentation.
I will only document the notable differences here.

## Notable differences to yesql 0.5 branch

* Connection is *always* first parameter to query function
* Connection cannot be given in defqueries phase
* Generate positional arguments when given :positional? true argument (for upgrade path from yesql <= 0.4 versions)
* No call-options map
* If SQL clause has no arguments, the function will have only 1 arity (the connection parameter)

## Positional arguments

The current yesql approach has moved away from positional arguments in favor of map arguments.
This is fine, but I have a large project with thousands of query fn calls and I wanted
to have an upgrade path that supports both ways.

Now passing in {:positional? true} argument to defqueries will also generate the
positional arguments.

```SQL
-- name: find-by-name-and-age-range
-- Test positional paremeter order with these 3 args
SELECT *
FROM person
WHERE name LIKE :name AND age >= :age_min AND age <= :age_max
```

Will generate a method that can be called in two ways:

```clojure

;; The map parameters still work as expected
(find-by-name-and-age-range db {:name "Foo%" :age_min 20 :age_max 40})

;; Positional arguments are provided in the order they appear in the SQL
(find-by-name-and-age-range db "Foo%" 20 40)
```


## Installation

Add this to your [Leiningen](https://github.com/technomancy/leiningen) `:dependencies`:

[![Clojars Project](http://clojars.org/jeesql/latest-version.svg)](http://clojars.org/jeesql)
