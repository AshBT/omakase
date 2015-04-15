(ns omakase.commands.status
  (:require [omakase.io :as okio]
            [omakase.utils :refer [defcommand]]
            [clojure.java.io :as io]))

(defcommand status
  "List known clouds."
  []
  (let [ok-dir-file (io/file okio/ok-dir)
        okdir?      (fn [path] (= okio/ok-dir (.getPath path)))
        cloud?      (fn [path] (and (.isDirectory path) (not (okdir? path))))]
    (->> (file-seq ok-dir-file)
         (filter cloud?)
         (map #(.getName %1))
         (clojure.pprint/pprint))))
