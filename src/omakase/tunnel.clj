(ns omakase.tunnel
  (:require [omakase.io :as okio]
            [clj-ssh.ssh :as ssh]
            [omakase.creds :refer [ssh-privkey]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]))

;; in a macro, x# generates a new "local" symbol, ~host splices in the variable
;; "host" and ~@ is unquote spliced, i.e., (def zebra [1 2 3]) then `(moose
;; ~@zebra) will be (quote (moose 1 2 3))

(defmacro with-ssh-tunnel
  "Creates an ssh tunnel context for a given cluster"
  [cluster-name host local-port remote-port & body]
  `(let [ssh-agent# (ssh/ssh-agent {})
         username#  "core"]
     (ssh/add-identity ssh-agent# {:private-key-path (ssh-privkey ~cluster-name)})
     (log/debug (format "Attempting ssh %s@%s" username# ~host))
     (let [session# (ssh/session ssh-agent# ~host {:strict-host-key-checking :no, :username username#})]
       (ssh/with-connection session#
         (log/debug (format "ssh %s@%s success" username# ~host))
         (log/info "Forwarding localhost port" ~local-port "to remote host" (format "%s@%s" username# ~host) "at port" ~remote-port)
         (try
           (ssh/with-local-port-forward [session# ~local-port ~remote-port]
             ~@body)
           (catch com.jcraft.jsch.JSchException e#  ; if the tunnel is already open...
              (log/info  "SSH tunnel appears to already exist. Attempting to reuse....")
              ~@body)
           (finally
             (log/debug "Closing SSH connection.")))))))


