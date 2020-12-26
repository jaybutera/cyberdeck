(ns cyberdeck.core
  (:gen-class)
  (:require [org.httpkit.server :as httpkit :refer [with-channel on-receive on-close send!]]
            [konserve.filestore :refer [new-fs-store]]
            [clojure.core.async :refer [<!!]]
            [konserve.core :as k]))

(try
  (def store (<!! (new-fs-store "./tm")))
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

(defn load-expr [fname data]
  "") ; Implement


(defn handler [req]
  (with-channel req channel
    (on-close channel (fn [status]
                        (println "Channel closed" status)))
    (on-receive channel (fn [msg]
                          (println msg)
                          (let [op (clojure.edn/read-string msg)]
                            (cond
                              (= (:action op) "save") (doall (map save-expr (:data op)))
                              (= (:action op) "load") (doall (map load-expr (:data op)))
                              :else (println "Unrecognized msg format:\n" msg)))
                          (send! channel msg)))))

(defn -main
  "Something useful?"
  [& args]
  (println "Starting cyberdeck on port 8080...")
  (httpkit/run-server handler {:port 8080}))
