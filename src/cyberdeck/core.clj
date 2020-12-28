(ns cyberdeck.core
  (:gen-class)
  (:require [org.httpkit.server :as httpkit :refer [with-channel on-receive on-close send!]]
            [konserve.filestore :refer [new-fs-store]]
            [clojure.core.async :as a :refer [<!!]]
            [konserve.core :as k]))

(try
  (def store (<!! (new-fs-store "./tmp")))
  (catch Exception e
    (println "Failed to establish new fs-store" e)))

(defn defn-name
  "Takes a parsed expression object and extracts the name from the data value.
  The expression is assumed to be a `defn`."
  [expr]
  (nth (:data expr) 1)) ; (defn x ..)

(defn save-expr
  "Write an s-expression object with its name as the key, the object as the value."
  [expr]
  (<!! (k/assoc store (:name expr) expr)))

(defn rm-expr
  "Dissociate an s-expression object from the database."
  [uuid]
  (<!! (k/dissoc store uuid)))

(defn load-expr
  [e-name]
  (<!! (k/get store e-name)))

(defn all-uuids []
  (let [c (k/keys store)
        key-set (<!! c)]
    (map #(:key %) key-set)))

(defn send-exprs
  "Send a list of expressions on a given channel."
  [channel es]
  (println "es" es)
  (send! channel (str {:action "load" :exprs es})))


(defn handler [req]
  (with-channel req channel
    (on-close channel (fn [status]
                        (println "Channel closed" status)))
    (on-receive channel (fn [msg]
                          (println msg)
                          (let [op (clojure.edn/read-string msg)]
                            (cond
                              (= (:action op) "save") (doall (map save-expr (:data op)))
                              (= (:action op) "delete") (doall (map rm-expr (:uuids op)))
                              (= (:action op) "load") (send-exprs channel (map load-expr (:names op)))
                              (= (:action op) "load-all") (send-exprs channel (map load-expr (all-uuids)))
                              :else (println "Unrecognized msg format:\n" msg)))))))

(defn -main
  [& args]
  (println "Starting cyberdeck on port 8080...")
  (httpkit/run-server handler {:port 8080}))
