(ns omakase.commands.config
  (:require [omakase.utils :refer [defcommand]]
            [omakase.io :as okio]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import  [org.jclouds.ssh.SshKeys]))

(defmulti write-ssh-keys class)

(defmethod write-ssh-keys com.google.common.collect.RegularImmutableMap
  [sshkeys]
  (let [pubkey-file  (io/file okio/ok-dir "global-key.pub")
        privkey-file (io/file okio/ok-dir "global-key")]
    (spit pubkey-file (.get sshkeys "public"))
    (spit privkey-file (.get sshkeys "private"))
    [pubkey-file privkey-file]))

(defmethod write-ssh-keys :default
  [sshkeys]
  (let [pubkey-file  (if (re-matches #"\.pub$" sshkeys)
                       (io/file sshkeys)
                       (io/file (str sshkeys ".pub")))
        privkey-file (io/file (string/replace sshkeys #"\.pub$" ""))]
    [pubkey-file privkey-file]))

(defcommand config
  "Configure omakase credentials."
  {:description
   (str "Omakase credentials can be set using this command or by directly editing "
        okio/config-file ".\n"
        "\n"
        "If a keyfile without an extension is provided, '.pub' is appended at the "
        "end to produce a public key. If an extension is provided, it is stripped "
        "to guess the private key.\n"
        "\n"
        "If no keyfile is provided, then omakase will generate one for you.\n\n"
        "This command will produce a file with the format\n\n"
        "    [keys]\n"
        "    private=/path/to/private/keys\n"
        "    public=/path/to/public/keys\n"
        "\n"
        "    [aws]\n"
        "    accesskey=AWS_ACCESS_KEY\n"
        "    secret=AWS_SECRET_KEY")
   :options [[nil "--keyfile KEYPATH" "Path to a keyfile (public or private)."]]}
  [[options] aws-access-key aws-secret-key]
  (let [sshkeys                    (or (:keyfile options) (org.jclouds.ssh.SshKeys/generate))
        [pubkey-file privkey-file] (write-ssh-keys sshkeys)]
    (okio/write-config aws-access-key aws-secret-key pubkey-file privkey-file)))

