(ns jeesql.positional-test
  (:require [expectations :refer :all]
            [clojure.java.jdbc :as jdbc]
            [jeesql.core :refer :all])
  (:import [java.sql SQLException SQLSyntaxErrorException SQLDataException]))

(def db {:subprotocol "derby"
         :subname (gensym "memory:")
         :create true})

(defqueries "jeesql/sample_files/acceptance_test_combined.sql"
  {:positional? true})

;; Create
(expect (create-person-table! db))

;; Insert -> Select.
(expect {:1 1M} (insert-person<! db "Alice" 20))
(expect {:1 2M} (insert-person<! db "Bob" 25))
(expect {:1 3M} (insert-person<! db "Charlie" 35))

(expect 3 (count (find-older-than db 10)))
(expect 2 (count (find-older-than db 20)))
(expect 1 (count (find-older-than db 26)))
(expect 0 (count (find-older-than db 42)))

(expect {:1 4M} (insert-person<! db {:age 666 :name "Santa"}))
(expect 4 (count (find-older-than db 1)))
(expect 1 (count (find-older-than db 100)))
(expect 1 (count (find-older-than db {:age 100})))
