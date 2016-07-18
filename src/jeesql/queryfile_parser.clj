(ns jeesql.queryfile-parser
  (:require [clojure.java.io :as io]
            [clojure.string :refer [join trim]]
            [jeesql.types :refer [map->Query]]
            [jeesql.util :refer [str-non-nil]]
            [clojure.string :as str]))

(def header-attribute #"^--\s*([^\s:]+):\s*(.+)$")
(def header-comment #"^--.*")
(def comment-line #"\s*--.*")

(defn- attribute-line? [line]
  (re-matches header-attribute line))

(defn- header-comment-line? [line]
  (re-matches header-comment line))

(defn- comment-line? [line]
  (re-matches comment-line line))

(defn- parse-header [header-lines]
  (reduce
   (fn [headers line]
     (let [[_ name val] (re-matches header-attribute line)]
       (if (and name val)
         (assoc headers (keyword name)
                (read-string val))
         headers)))
   {}
   header-lines))

(defn parse [lines]
  (let [lines (drop-while (comp not attribute-line?) lines)
        [header lines] (split-with attribute-line? lines)
        [comment lines] (split-with header-comment-line? lines)
        [statement lines] (split-with (comp not attribute-line?) lines)
        attributes (parse-header header)]
    (if-not (:name attributes)
      (throw (ex-info "Parse error: query must have a name" {}))
      [{:name (name (:name attributes))
        :docstring (->> comment
                        (map #(str/replace (str/trim %) #"^--\s*" ""))
                        (str/join "\n"))
        :statement (->> statement
                        (remove comment-line?)
                        (str/join "\n")
                        str/trim)
        :attributes (dissoc attributes :name)}
       lines])))

(defn parse-all [lines]
  (loop [queries []
         lines lines]
    (if (empty? lines)
      queries
      (let [[query lines] (parse lines)]
        (recur (conj queries query)
               lines)))))



(defn parse-tagged-queries
  "Parses a string with Jeesql's defqueries syntax into a sequence of maps."
  [text]
  (mapv map->Query (parse-all (str/split-lines text))))
