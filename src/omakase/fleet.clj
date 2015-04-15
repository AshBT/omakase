(ns omakase.fleet
  (:require [org.jclouds.compute2 :as jclouds]
            [omakase.tunnel :as tunnel]
            [omakase.utils :as utils]
            [clojure.java.shell :as shell]
            [clojure.tools.logging :as log]
            [clj-http.client :as http]))

;; in a macro, x# generates a new "local" symbol, ~host splices in the variable
;; "host" and ~@ is unquote spliced, i.e., (def zebra [1 2 3]) then `(moose
;; ~@zebra) will be (quote (moose 1 2 3))

(defmacro with-fleet-ssh-tunnel
  "Creates an ssh tunnel context to query the fleet API for a given cluster"
  [cluster-name & body]
  `(tunnel/with-ssh-tunnel ~cluster-name (utils/cluster-ip ~cluster-name) 4001 49153 ~@body))

(defn node-ip
  "Gets the IP address of a jclouds node."
  [node]
  (-> node jclouds/public-ips seq first))

(defn discovery
  "Connects to the Fleet API's discovery URL for the given cluster."
  [cluster-name]
  (with-fleet-ssh-tunnel cluster-name
    ((http/get "http://localhost:4001/fleet/v1/discovery") :body)))

;; the code below is used to execute fleetctl locally to start services
;; the reason for this code is because of issue #5 which i wish to tackle
;; in the future
(defn fleetctl
  "Uses fleetctl to run a fleet command. Requires that the fleet endpoint
   be available at localhost:4001. This can be accomplished by wrapping
   this function with `with-fleet-ssh-tunnel`."
  [cmd & args]
  (let [fleetcmd `("fleetctl" "--endpoint" "http://localhost:4001" "--driver=API" ~cmd ~@args)]
    (log/debug "Running fleetctl command" fleetcmd)
    (let [shell-result (apply shell/sh fleetcmd)]
      (log/debug (str shell-result))
      (case (:exit shell-result)
        0   (do
              (log/debug "Fleetctl command successful")
              (log/debug (str "\n" (:out shell-result)))
              (:out shell-result))
        (do
          (log/debug "Fleetctl command failed with exit code" (:exit shell-result))
          (log/debug (str "\n" (:err shell-result)))
          :app/fail)))))

