(ns jeesql.acceptance-test
  (:require [expectations :refer :all]
            [clojure.java.jdbc :as jdbc]
            [jeesql.core :refer :all])
  (:import [java.sql SQLException SQLSyntaxErrorException SQLDataException]))

(def derby-db {:subprotocol "derby"
               :subname (gensym "memory:")
               :create true})

;;; Single query.
(defquery current-time
  "jeesql/sample_files/acceptance_test_single.sql")

(expect java.util.Date
        (-> (current-time derby-db)
            first
            :time))

;;; Multiple-query workflow.
(defqueries
  "jeesql/sample_files/acceptance_test_combined.sql")

;; Create
(expect (create-person-table! derby-db))

;; Insert -> Select.
(expect {:1 1M} (insert-person<! derby-db {:name "Alice"
                                           :age 20}))
(expect {:1 2M} (insert-person<! derby-db {:name "Bob"
                                           :age 25}))
(expect {:1 3M} (insert-person<! derby-db {:name "Charlie"
                                           :age 35}))

(expect 3 (count (find-older-than derby-db {:age 10})))
(expect 1 (count (find-older-than derby-db {:age 30})))
(expect 0 (count (find-older-than derby-db {:age 50})))

;;; Select with IN.
(expect 2 (count (find-by-age derby-db {:age [20 35]})))

;; Update -> Select.
(expect 1 (update-age! derby-db {:age 38
                                 :name "Alice"}))
(expect 0 (update-age! derby-db {:age 38
                                 :name "David"}))

(expect 3 (count (find-older-than derby-db {:age 10})))
(expect 2 (count (find-older-than derby-db {:age 30})))
(expect 0 (count (find-older-than derby-db {:age 50})))

;; Delete -> Select.
(expect 1 (delete-person! derby-db {:name "Alice"}))

(expect 2 (count (find-older-than derby-db {:age 10})))
(expect 1 (count (find-older-than derby-db {:age 30})))
(expect 0 (count (find-older-than derby-db {:age 50})))

;; Failing transaction: Insert with abort.
;; Insert two rows in a transaction. The second throws a deliberate error, meaning no new rows created.
(expect 2 (count (find-older-than derby-db {:age 10})))

(expect SQLException
        (jdbc/with-db-transaction [connection derby-db]
          (insert-person<! connection {:name "Eamonn"
                                       :age 20})
          (insert-person<! connection {:name "Bob"
                                       :age 25} )))

(expect 2
        (count (find-older-than derby-db {:age 10})))

;;; Type error.
(expect SQLDataException
        (insert-person<! derby-db {:name 5
                                   :age "Eamonn"}))

;; Drop
(expect (drop-person-table! derby-db))

;; Syntax error handling.
(defquery syntax-error
  "jeesql/sample_files/syntax_error.sql")

(expect SQLSyntaxErrorException
        (syntax-error derby-db))
