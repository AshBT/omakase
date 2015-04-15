(ns omakase.commands.create
  (:require [omakase.utils :refer [defcommand]]
            [omakase.io :as okio]
            [omakase.services :as service]
            [clojure.java.io :as io]))

(defn write-base-file
  "Writes config files to a cluster's working directory.

   If given the cluster-name and a dest filename, will attempt to construct
   the source by looking in resources.

   Otherwise, writes the source out to the destination. Also inserts AWS keys if needed."
  ([cluster-name dest]
   (write-base-file cluster-name dest (io/resource dest)))
  ([cluster-name dest src]
   (let [ok-dest  (okio/cluster-file cluster-name dest)
         {{{:keys  [accesskey secret]} :global} :aws}  (okio/read-config)
         contents (-> src
                      slurp
                      (clojure.string/replace #"#AWS_KEY#" accesskey)
                      (clojure.string/replace #"#AWS_SECRET#" secret))]
     (okio/ok-spit ok-dest contents))))

(defcommand create
  "Create the cloud with the name 'cluster-name'."
  {:description "Creates a cloud on amazon AWS with the provided credentials."}
  [cluster-name]
  (let [services (ns-publics 'omakase.services)]
    (doseq [[_ variable] services]
      (write-base-file cluster-name @variable))))
