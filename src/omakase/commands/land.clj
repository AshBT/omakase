(ns omakase.commands.land
  (:require [omakase.utils :refer [defcommand]]
            [omakase.creds :as creds]
            [clojure.java.io :as io]
            [org.jclouds.compute2 :as jclouds]
            [clojure.tools.logging :as log]))

(defcommand land
  "Shuts down the named cluster."
  [cluster-name]
  (do
    (log/debug "Gently landing" cluster-name)
    (let [aws       (creds/aws-cluster cluster-name)
          responses (jclouds/destroy-nodes-matching aws (jclouds/in-group? cluster-name))]
      (map #(log/debug (format "<< %s%n" %)) responses)
      (log/debug (format "'%s'" cluster-name) "has landed.")
      responses)))


