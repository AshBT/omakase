(ns omakase.commands.run
  (:require [omakase.utils :as utils]
            [omakase.tunnel :as tunnel]
            [clojure.java.shell2 :as sh]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.string :as string]))


(utils/defcommand run
  "Run an app with omakase environment variables."
  {:description (str "The 'run' command is prepended to any command line script or tool you wish to "
                     "run on your local machine. It will set the following environment variables:\n\n"
                     "    KAFKA_BROKER\n"
                     "    PYTHONUNBUFFERED\n"
                     "    ZOOKEEPER_ZERVER\n\n"
                     "These environment variables are set before running your command. Thus,\n\n"
                     "    ok run foobar python myapp.py\n\n"
                     "will run with the ability to connect to your remote cluster's Kafka and Zookeeper instances.\n\n"
                     "Note that this command sets up several local ssh tunnels to access the private "
                     "resources on your cloud.")
   :in-order true}
  [cluster-name & cmds]
  (if (seq cmds)
    (sh/with-sh-env {:KAFKA_BROKER "localhost:9092",
                     :PYTHONUNBUFFERED "YES"
                     :ZOOKEEPER_SERVER "localhost:2181"}
      (let [runcmd    `(~@cmds :err :pass :out :pass)
            kafka-ip  (utils/cluster-kafka-ip cluster-name)
            zk-ip     (utils/cluster-zk-ip cluster-name)]
        (tunnel/with-ssh-tunnel cluster-name kafka-ip 9092 9092
          (tunnel/with-ssh-tunnel cluster-name zk-ip 2181 2181
            (apply sh/sh runcmd)))))
    (utils/show-msg "No commands provided for running." :app/fail)))
