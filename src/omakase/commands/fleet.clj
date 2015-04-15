(ns omakase.commands.fleet
  (:require [omakase.utils :refer [defcommand]]
            [omakase.fleet :as fleet]))

(defcommand fleet
  "Forwards command to fleetctl."
  {:description
  (str
   "This command creates an ssh tunnel to the given cluster in order to run the commands for fleetctl. "
   "As an example,\n\n"
   "    ok fleet foobar list-machines\n\n"
   "will open an ssh tunnel to the foobar cluster and use fleetctl to list the machines.\n\n"
   "This command is mostly useful for debugging services. Note that commands that require ssh access will fail, "
   "since omakase does not attempt to add the generated keys to your ssh agent.")
   :in-order true}
  [cluster-name & fleet-args]
  (fleet/with-fleet-ssh-tunnel cluster-name
    (apply fleet/fleetctl fleet-args)))
