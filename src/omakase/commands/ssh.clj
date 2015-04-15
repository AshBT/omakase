(ns omakase.commands.ssh
  (:require [clojure.java.shell :refer [sh]]
            [clojure.tools.logging :as log]
            [omakase.io :refer [cluster-file]]
            [omakase.utils :refer [defcommand]]
            [omakase.creds :refer [ssh-privkey]]))

(defcommand ssh
  "Prints the ssh command to ssh into (one machine in) the cloud."
  [cluster-name]
  (let [ip  (slurp (cluster-file cluster-name  "ip"))
        cmd `("ssh" "-i" ~(ssh-privkey cluster-name) ~(str "core@" ip))]
    (log/info cmd)))
