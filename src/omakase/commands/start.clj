(ns omakase.commands.start
  (:require [omakase.utils :refer [defcommand]]
            [omakase.fleet :as fleet]
            [org.jclouds.compute2 :as jclouds]
            [clj-http.client :as http]
            [clojure.tools.logging :as log]))

(defcommand start
  "Starts a service."
  {:description "Starts a service on the named cluster. The service is specified by a path to the service file."}
  [cluster-name & service-paths]
  (fleet/with-fleet-ssh-tunnel cluster-name
    (apply fleet/fleetctl "start" service-paths)))

