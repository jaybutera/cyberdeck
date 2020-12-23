(ns cyberdeck.core
  (:gen-class)
  (:use [org.httpkit.server :as httpkit]))

(def STORAGE_FILE "tmp.cbd")

(defn defn-name [expr]
  "Takes a parsed expression object and extracts the name from the data value.
  The expression is assumed to be a `defn`."
  (println "expr::" expr)
  (nth (:data expr) 1)) ; (defn x ..)

(defn save-to-file [fname data]
  (let [db (clojure.edn/read-string (slurp fname))
        data-name (defn-name (first data))
        new-db (assoc db data-name data)]
    (spit fname (str new-db))))

(defn load-from-file [fname data]
  "") ; Implement


(defn handler [req]
  (with-channel req channel
    (on-close channel (fn [status]
                        (println "Channel closed" status)))
    (on-receive channel (fn [msg]
                          (println msg)
                          (let [op (clojure.edn/read-string msg)]
                            (cond
                              (= (:action op) "save") (save-to-file STORAGE_FILE (:data op))
                              (= (:action op) "load") (load-from-file STORAGE_FILE (:data op))))
                          (send! channel msg)))))

(defn -main
  "Something useful?"
  [& args]
  (println "Starting cyberdeck on port 8080...")
  (httpkit/run-server handler {:port 8080}))
