(ns omakase.commands.version
  (:require [omakase.utils :refer [defcommand]]
            [clojure.java.shell :refer [sh]]))

;; the following *must be* macros in order for leiningen to compile them into
;; the uberjar
(defmacro get-version []
  (System/getProperty "omakase.version"))

(defmacro get-git-sha []
  (try
    (let [{:keys [err exit out]} (sh "git" "rev-parse" "HEAD")]
      (if (> exit 0) "unknown" out))
    (catch Exception e ("unknown"))))

(defcommand version
  "Provide versioning information."
  []
  (println "Omakase!")
  (println)
  (println (str "Version:    " (get-version)))
  (println (str "Git commit: " (get-git-sha))))

