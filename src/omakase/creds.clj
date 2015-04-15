(ns omakase.creds
  (:require [org.jclouds.compute2 :as jclouds]
            [omakase.io :as okio]))

(def ^:private provider "aws-ec2")

(defn aws-cluster
  "Set the AWS compute service for this cloud. The cluster-name doesn't do anything, yet."
  [cluster-name]
  (let [{{{:keys [accesskey secret]} :global} :aws} (okio/read-config)]
    (jclouds/compute-service provider accesskey secret :sshj :slf4j)))

(defn ssh-pubkey
  "Get the pubkey file for this cloud."
  [cluster-name]
  (let [{{{:keys [public]} :global} :keys} (okio/read-config)]
    public))

(defn ssh-privkey
  "Get the private key file for this cloud."
  [cluster-name]
  (let [{{{:keys [private]} :global} :keys} (okio/read-config)]
    private))

