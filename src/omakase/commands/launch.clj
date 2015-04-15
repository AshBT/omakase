(ns omakase.commands.launch
  (:require [omakase.utils :refer [defcommand]]
            [omakase.fleet :as fleet]
            [omakase.io :as okio]
            [omakase.services :as service]
            [omakase.creds :as creds]
            [clojure.java.io :as io]
            [org.jclouds.compute2 :as jclouds]
            [clj-http.client :as http]
            [clojure.tools.logging :as log]))

;; TODO options -
;;    ec2 accesskey
;;    ec2 secret
;;    cloud name
;;    verbose logging?
;;    path to public key
;;    which coreOS channel to use (stable, alpha, beta)--only allow alpha +
;;        beta first
(def cloud-config
  (io/resource "coreos-cloud-config"))

; TODO: check that status = 200
(defn coreos-discovery!
  "Get a new coreos discovery token. If no URL is provided, will
   default to https://discovery.etcd.io/new"
  ([]
   (coreos-discovery! "https://discovery.etcd.io/new"))
  ([discovery-url]
   (-> discovery-url http/get :body)))

(defn coreos-cloud-config
  "Create the cloud-config file needed to be uploaded for coreos initialization"
  [discovery-token]
  (-> cloud-config slurp (format discovery-token)))

; the ssh key should be set up with a config file also
(defn create-coreos-opts
  "creates the jclouds options needed to start a new coreos cluster.
   also creates a new etcd discovery token.

   in the future, will allow you to choose region and pubkey location.
   will also let you choose to use :stable, :beta, or :alpha coreOS?"
  [path-to-pubkey cloud-config]
  {:image-id              "us-west-1/ami-83d533c7"
   :inbound-ports         [80 22]
   :block-on-port         [22 120]
   :override-login-user   "core"
   :authorize-public-key  (slurp path-to-pubkey)
   :user-data             (.getBytes cloud-config)
   :hardware-id           "m3.medium"
   :tags                  ["memex"]})

(defn create-coreos-nodes
  "starts a number of coreos nodes on amazon ec2 west; coreos-opts is a
   dictionary containing valid options from
   org.jclouds.compute2/known-template-options.

   while you can construct your own, we expect the options output by
   create-coreos-opts.

   returns a seq of org.jclouds.compute2 node objects."
  [cluster-name num-nodes coreos-opts]
  (let [aws         (creds/aws-cluster cluster-name)
        core-os-ami (jclouds/build-template aws coreos-opts)]
    (log/debug "attempting to start" num-nodes "aws core os image(s)")
    (let [result (jclouds/create-nodes aws cluster-name num-nodes core-os-ami)]
      (log/info "started" num-nodes "node(s)")
      (log/debug result)
      result)))

(defn start-cluster
  "Start cluster with name cluster-name and number of nodes"
  [cluster-name number]
  (let [discovery     (coreos-discovery!)
        cloud-config  (coreos-cloud-config discovery)
        pubkey        (creds/ssh-pubkey cluster-name)
        coreos-opts   (create-coreos-opts pubkey cloud-config)
        file          (okio/cluster-file cluster-name "cloud-config")]
    (log/info "Writing cloud-config to" file)
    (okio/ok-spit file cloud-config)
    (log/info "Starting" number "node(s). Will take some time; please be patient.")
    (create-coreos-nodes cluster-name number coreos-opts)))

(defn ^:private apply-template
  "Apply a template from the cluster-name cluster 'number' times. Use the function
   f to generate the result.

   The function f must take as arguments the id (a number), then instance (a File)
   of the template, and the contents of the template."
  [cluster-name template number f]
  (let [gen-dir   (okio/cluster-file cluster-name "gen")
        contents  (slurp template)
        basename  (.getName (io/file template))
        prefix    (first (clojure.string/split basename #"\."))]
    (log/debug "generating instances for" template)
    (okio/remove-files gen-dir)
    (for [id (range number)]
      (let [instance (io/file gen-dir (str prefix (inc id) ".service"))]
        (f id instance contents)
        (str instance)))))

(defn generate-services
  ([cluster-name] (generate-services cluster-name 3 3))
  ([cluster-name num-zookeeper num-kafka]
   (let [kafka          #{"kafka-broker" "kafka-broker-discovery"}
         zookeeper      #{"zookeeper", "zookeeper-discovery"}
         get-path       (fn [[_ variable]] (->> @variable
                                                (okio/cluster-file cluster-name)
                                                (.getPath)))
         service-type   (fn [[k _]] (cond
                                      (contains? kafka (name k))        :kafka
                                      (contains? zookeeper (name k))    :zookeeper
                                      :else                             :service))
         write-file     (fn [_ instance contents] (okio/ok-spit instance contents))
         gen-template   (fn [services items number f]
                          (apply concat services (for [template items]
                                                   (apply-template cluster-name (get-path template) number f))))
         service-map    (group-by service-type (ns-publics 'omakase.services))]
     (-> (map get-path (:service service-map))
         (gen-template (:kafka service-map) num-kafka write-file)
         (gen-template (:zookeeper service-map) num-zookeeper write-file)))))

(defn start-basic-services
  "Start the basic services."
  [cluster-name]
  (Thread/sleep 5000)
  (log/debug "Starting basic services")
  (let [services (doall (generate-services cluster-name))]
    (fleet/with-fleet-ssh-tunnel cluster-name
      (case (apply fleet/fleetctl "start" services)
        :app/fail (log/debug "error in starting services")
        (log/debug "basic services started")))))

(def ^:private launch-options
  [["-n" "--number NUM" "Number of machines"
    :default 5
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]])

(defcommand launch
  "Launch a cluster with a given name and number of nodes."
  {:options launch-options}
  [[options] cluster-name]
  (if (okio/cluster-exists? cluster-name)
    (let [num-nodes (options :number)
          nodes     (start-cluster cluster-name num-nodes)
          ip        (fleet/node-ip (first nodes))
          ip-info   (okio/cluster-file cluster-name "ip")]
      (okio/ok-spit ip-info ip)
      ;(log/debug "sleeping for 60 seconds to wait for nodes to start")
      ;(Thread/sleep 60000)
      (start-basic-services cluster-name)
      (log/info "Connect to a machine in the cluser with:" (str "ssh -i " (creds/ssh-pubkey cluster-name) " core@" ip)))
    (omakase.utils/show-msg "Before launching a cluster, please create a cluster with 'ok create'." :app/fail)))
    ;(throw (Exception. "Before launching, please run 'ok add' to add a new cluster.")))

