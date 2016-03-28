# Jeesql - a fork of yesql with an upgrade path

Jeesql is a Clojure library for _using_ SQL. Please see krisajenkins/yesql for yesql documentation.
I will only document the notable differences here.

## Notable differences to yesql 0.5 branch

* Connection is *always* first parameter to query function
* Connection cannot be given in defqueries phase
* Generate positional arguments when given :positional? true argument (for upgrade path from yesql <= 0.4 versions)
* No call-options map
* If SQL clause has no arguments, the function will have only 1 arity (the connection parameter)
* Query attributes in SQL files (see below)
* defquery support is removed (only defqueries supported)
* automatic reload of defqueries files

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

## Query attributes

Jeesql adds the possibility to annotate queries with attributes that change the
way the query is processed. I think this is better to do at the query site instead
of at the call site (with yesql's call-options).

Attributes are placed between the name line and the docstring.

Currently two attributes are supported: single? and return-keys.


### single?

Single? attribute (for selects) changes the result set processing to return a
single value.

```SQL
-- name: count-people-older-than
-- single?: true
-- Count how many people are older than the given age
SELECT COUNT(*) FROM people WHERE age > :age
```

Will generate a function that returns a the count number as a single value when
called, instead of a sequence of maps.

### return-keys

Return keys (for insert) is a vector of strings for keys to return.
This is mainly for Oracle users.

```SQL
-- name: insert-foo<!
-- return-keys: ["FOO_ID"]
-- Insert a foo and return FOO_ID
INSERT INTO foo (bar) VALUES (:bar)
```

Will generate a function that sets the prepared statement return keys
attribute.

## Automatic reload

Calls to defqueries will register the files for watching. If the file changes the
file is reloaded. This removes the need to constantly switch between the SQL file
and the Clojure file to eval again when developing.

## Installation

Add this to your [Leiningen](https://github.com/technomancy/leiningen) `:dependencies`:


[![Clojars Project](http://clojars.org/webjure/jeesql/latest-version.svg)](http://clojars.org/webjure/jeesql)
