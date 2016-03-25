(ns jeesql.autoreload
  "Support automatic reloading when in development mode. If classpath resources are
  file URLs, watches changes in them."
  (:require [clojure.java.io :as io])
  (:import [java.util.concurrent Executors TimeUnit ThreadFactory]))

;; Tried using Java nio WatchService but the API was horrible and the
;; notifications arrived way too slowly... and it was polling on OS X.
;; So we might as well just implement our own simpler polling.


;; The watch agent has is a mapping of file url to
;; file information. :timestamp, :reload-fn
(def watch-agent (agent {}))

(defn- watch-file
  "Ensure that the given SQL file is being watched"
  [files file-url reload-fn]
  (if (= "file" (.getScheme (.toURI file-url)))
    (assoc files file-url
           {:timestamp (System/currentTimeMillis)
            :reload-fn reload-fn})

    ;; Not a file URI, I can't watch this
    files))

(defn- reload-if-changed [[file-url {:keys [timestamp reload-fn] :as file}]]
  (let [modified (.lastModified (io/as-file file-url))]
    (if (> modified timestamp)
      (do (try
            (println "RELOADING " (str file-url))
            (reload-fn (slurp file-url))
            (catch Exception e
              (println "ERROR RELOADING " file-url "\n" e)))
          [file-url (assoc file :timestamp (System/currentTimeMillis))])
      [file-url file])))

(defn- check-for-reload [files]
  (into {}
        (map reload-if-changed)
        (seq files)))

(defn autoreload
  "Register file URL for autoreload. When file changes the reload-fn will be invoked
with the file contents.
  If the URL is not a file: URL, it cannot be reloaded and is ignored."
  [file-url reload-fn]
  (send watch-agent watch-file file-url reload-fn))

(def reload-poll-ms 2000)
(defonce reloader (atom nil))

(defn start-autoreload []
  (swap! reloader
         (fn [reloader]
           (or reloader
               (doto (Executors/newSingleThreadScheduledExecutor)
                 (.scheduleAtFixedRate #(send watch-agent check-for-reload)
                                       reload-poll-ms reload-poll-ms
                                       TimeUnit/MILLISECONDS))))))
(defn stop-autoreload []
  (swap! reloader
         #(when %
            (.shutdownNow %)
            nil)))

(when-not *compile-files*
  (start-autoreload))
