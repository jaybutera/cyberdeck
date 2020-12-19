(ns cyberdeck.core
  (:gen-class)
  (:use [org.httpkit.server :as httpkit]))

(defn handler [req]
  (with-channel req channel
    (on-close channel (fn [status]
                        (println "Channel closed" status)))
    (on-receive channel (fn [msg]
                          (println msg)
                          (send! channel msg)))))

(defn -main
  "Something useful?"
  [& args]
  (println "Starting cyberdeck on port 8080...")
  (httpkit/run-server handler {:port 8080}))
