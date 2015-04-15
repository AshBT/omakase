(ns omakase.io
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.logging :as log]))

(defmulti contents class)
(defmethod contents java.io.File [data]
  (slurp data))
(defmethod contents java.net.URL [data]
  (slurp data))
(defmethod contents :default [data]
  data)

(def ^:const ok-dir (str (System/getProperty "user.home") "/.ok"))

(defn cluster-file
  [cluster-name filename]
  (io/file ok-dir cluster-name filename))

(defn remove-files
  "Unsafely deletes all files under the path."
  [path]
  (when (and (.exists path) (.isDirectory path))
    (doseq [file (file-seq path) :when (.isFile file)]
      (io/delete-file file))))

(defn cluster-services?
  "Checks that all the services defined in omakase.services exist in the cloud
   directory."
  [cluster-name]
  (let [cluster-file-exists?  #(->> @%1
                                  (cluster-file cluster-name)
                                  (.exists))]
    (every? cluster-file-exists? (vals (ns-publics 'omakase.services)))))

(defn cluster-exists?
  "Checks that the cluster exists. No error reporting / suggesting yet."
  [cluster-name]
  (let [cluster-dir?      (.isDirectory (io/file ok-dir cluster-name))
        cluster-services? (cluster-services? cluster-name)]
    (and cluster-dir? cluster-services?)))

(defn ok-spit
  "Write a file with the given path. Will create the
   directory if it doesn't exist."
  [file data]
  (when (io/make-parents file)
    (log/info "Created top-level directories for" (.getPath file)))
  (log/info "Writing" (.getPath file))
  (spit file (contents data)))

(def config-file (io/file ok-dir "config"))

(defn ^:private create-nested-hashmap
  [section-map-seq]
  (if-let [[section subsection & the-rest] section-map-seq]
    {section {subsection (apply hash-map the-rest)}}))

(defn parse-config
  "Matches text of the form
      [section-header \"optional-subsection\"]
      key1=value1
      key2=value2
   and returns a hash map."
  ([config-seq] (parse-config {} nil config-seq))
  ([config section-map-seq config-seq]
   (if (seq config-seq)
     (if-let [[_ section subsection] (re-matches #"\[\s*(\w+)(?:\s+\"(\w+)\")?\s*\]" (first config-seq))]
       (let [config (into config (create-nested-hashmap section-map-seq))
             section-map-seq [(keyword section) (or (keyword subsection) :global)]]
         (recur config section-map-seq (rest config-seq)))
       (if-let [[_ mykey myval] (re-matches #"\s*(\w+)\s*=\s*([\-\\\/\.\~\w]+)\s*" (first config-seq))]
         (recur config (conj section-map-seq (keyword mykey) myval) (rest config-seq))
         (recur config section-map-seq (rest config-seq))))
     (into config (create-nested-hashmap section-map-seq)))))

(defn read-config
  "Read an existing omakase config file. Returns a map of key-val pairs."
  []
  (with-open [reader (io/reader config-file)]
    (parse-config (line-seq reader))))

(defn write-config
  "Write a new omakase config file."
  [aws-accesskey aws-secretkey pubkey-path privkey-path]
  (with-open [writer (io/writer config-file)]
    (let [data  ["[keys]"
                 (format "public=%s" (.getPath pubkey-path))
                 (format "private=%s" (.getPath privkey-path))
                 ""
                 "[aws]"
                 (format "accesskey=%s" aws-accesskey)
                 (format "secret=%s" aws-secretkey)]]
      (log/debug "Writing your info to config file at" (.getPath config-file))
      (.write writer (string/join \newline data))
      (log/debug "Done writing config file at" (.getPath config-file)))))
