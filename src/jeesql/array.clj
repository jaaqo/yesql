(ns jeesql.array
  "Support for SQL array parameters. Require this namespace to have seqable values turned
  into JDBC array parameters."
  (:require [clojure.java.jdbc :as jdbc])
  (:import [java.sql PreparedStatement]))

(def +sql-type+
  {java.lang.Long "INTEGER"
   java.lang.String "VARCHAR"})

(defn- to-sql-array [^PreparedStatement ps coll]
  (let [java-array (into-array coll)
        array-type (.getComponentType (.getClass java-array))
        sql-type (get +sql-type+ array-type)]
    (if-not sql-type
      (throw (RuntimeException. (str "Unable to determine SQL type for Java type: " sql-type)))
      (.createArrayOf (.getConnection ps)
                      sql-type
                      java-array))))


(extend-protocol jdbc/ISQLParameter
  clojure.lang.Seqable
  (set-parameter [v ^PreparedStatement s ^long i]
    (.setArray s i
               (to-sql-array s v))))
