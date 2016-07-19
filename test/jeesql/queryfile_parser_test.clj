(ns jeesql.queryfile-parser-test
  (:require [clojure.string :refer [join]]
            [clojure.template :refer [do-template]]
            [expectations :refer :all]
            [jeesql.queryfile-parser :refer :all]
            [jeesql.types :refer [map->Query]]
            [jeesql.util :refer [slurp-from-classpath]])
  (:import [clojure.lang ExceptionInfo]))

(expect
 [(map->Query {:name "the-time"
               :docstring "This is another time query.\nExciting, huh?"
               :statement "SELECT CURRENT_TIMESTAMP\nFROM SYSIBM.SYSDUMMY1"
               :attributes {}})
  (map->Query {:name "sums"
               :docstring "Just in case you've forgotten\nI made you a sum."
               :statement (join "\n" ["SELECT"
                                      "    :a + 1 adder,"
                                      "    :b - 1 subtractor"
                                      "FROM SYSIBM.SYSDUMMY1"])
               :attributes {}})
  (map->Query {:name "edge"
               :docstring "And here's an edge case.\nComments in the middle of the query."
               :statement (join "\n" ["SELECT"
                                      "    1 + 1 AS two"
                                      "FROM SYSIBM.SYSDUMMY1"])
               :attributes {}})
  (map->Query {:name "query-with-default"
               :docstring "This query has default parameter foo"
               :statement "SELECT * FROM foobar WHERE foo = :foo AND bar = :bar"
               :attributes {:default-parameters {:foo 42}}})
  ]
 (parse-tagged-queries (slurp-from-classpath "jeesql/sample_files/combined_file.sql")))

;;; Failures.
(expect #"Parse error"
        (try
          (parse-tagged-queries (slurp-from-classpath "jeesql/sample_files/tagged_no_name.sql"))
          (catch ExceptionInfo e (.getMessage e))))

(expect #"name: the-time2"
        (-> "jeesql/sample_files/tagged_two_names.sql"
            slurp-from-classpath
            parse-tagged-queries
            first :docstring))

;;; Parsing edge cases.

(expect ["this-has-trailing-whitespace"]
        (map :name
             (parse-tagged-queries (slurp-from-classpath "jeesql/sample_files/parser_edge_cases.sql"))))


;;; Parse queries with attributes
(expect {:meaning-of-life 42 :result-type :single}
        (->  "jeesql/sample_files/queries_with_attributes.sql"
             slurp-from-classpath
             parse-tagged-queries
             first
             :attributes))
