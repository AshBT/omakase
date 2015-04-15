(ns omakase.services)
;;; This file contains the files that should exist for every cluster.
;;; A cluster "exists" (or can be reproduced) if all these files exist.

;;; TODO: there must be a way to simple look for *.service files in the
;;; resource directory?
(def ^:const kafka-broker "kafka-broker@.service")

(def ^:const kafka-broker-discovery "kafka-broker-discovery@.service")

(def ^:const zookeeper "zookeeper@.service")

(def ^:const zookeeper-discovery "zookeeper-discovery@.service")

; (def ^:const nginx-proxy "nginx-proxy.service")

(def ^:const docker-registry "docker-registry.service")
