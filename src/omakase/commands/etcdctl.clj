(ns omakase.commands.etcdctl
  (:require [omakase.utils :refer [defcommand] :as utils]
            [omakase.tunnel :as tunnel]
            [clojure.tools.logging :as log]
            [clojure.java.shell :as shell]))

;; TODO: this is very similar to the fleet 'forwarder', so we should probably
;; make a template of it...
(defn etcd
  "Uses etcdctl to run an etcdctl command. Requires that the etcd endpoint
   be available at localhost:4001. This can be accomplished by wrapping
   this function with `with-ssh-tunnel cluster-name 4001 4001`."
  [cmd & args]
  (let [etcdcmd `("etcdctl" "--no-sync" ~cmd ~@args)]
    (log/debug "Running etcdctl command" etcdcmd)
    (let [shell-result (apply shell/sh etcdcmd)]
      (log/debug (str shell-result))
      (case (:exit shell-result)
        0   (do
              (log/debug "Etcdctl command successful")
              (log/debug (str "\n" (:out shell-result)))
              :app/ok)
        (do
          (log/debug "Etcdctl command failed with exit code" (:exit shell-result))
          (log/debug (str "\n" (:err shell-result)))
          :app/fail)))))


(defcommand etcdctl
  "Forwards command to etcdctl."
  {:description
  (str
   "This command creates an ssh tunnel to the given cluster in order to run the commands for etcdctl. "
   "As an example,\n\n"
   "    ok etcdctl foobar ls --recursive\n\n"
   "will open an ssh tunnel to the foobar cluster and use etcdctl to list keys.\n\n"
   "This command is mostly useful for debugging etcd.")
   :in-order true}
  [cluster-name & etcdctl-args]
  (tunnel/with-ssh-tunnel cluster-name (utils/cluster-ip cluster-name) 4001 4001
    (apply etcd etcdctl-args)))

