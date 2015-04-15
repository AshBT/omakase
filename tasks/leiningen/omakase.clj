(ns leiningen.omakase
  "Install omakase to ~/.ok!"
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [leiningen.core.main :refer [info, warn, debug]]
            [leiningen.uberjar :as uberjar]))

(defmacro oma-info
  [& body]
  `(info (str ~@(interpose " " body))))

(defn compile-uberjar [project main]
  (info "Compiling omakase uberjar. Please be patient!")
  (uberjar/uberjar project main))

(defn inject-version [source-bin version]
  (oma-info "Setting omakase version in" source-bin "to" version)
  (string/replace (slurp source-bin) #"%%version%%" version))

(defn omakase
  "# Omakase
   This command installs the omakase uberjar to ~/.ok. It also adds the script
   `bin/ok` to the :omakase-install-dir, which defaults to `/usr/local/bin`.

   Will require sudo privileges if install fails."
  [project & args]
  (let [install-dir (io/file (get project :omakase-install-dir "/usr/local/bin"))]
    (if (.canWrite install-dir)
      (let [standalone  (io/file (compile-uberjar project (first args)))
            source-bin  (io/file (project :root) "bin" "ok")
            target-bin  (io/file install-dir "ok")
            user-ok-jar (io/file (System/getProperty "user.home") ".ok" (.getName standalone))]
        (oma-info "Deleting existing" standalone)
        (if (io/delete-file user-ok-jar)
          (oma-info "Existing" standalone "deleted.")
          (oma-info "No existing" standalone "found."))
        (oma-info "Copying" standalone "to" user-ok-jar)
        (io/make-parents user-ok-jar)
        (io/copy standalone user-ok-jar)
        (oma-info "Moving" source-bin "to" install-dir)
        (io/make-parents target-bin)
        (io/copy (inject-version source-bin (project :version)) target-bin)
        (oma-info "Changing permissions on" target-bin)
        (.setExecutable target-bin true false)
        (oma-info "Completed. Be sure" install-dir "is in your path."))
      (warn "Cannot write to" (str install-dir ".") "Try 'sudo'?"))))

