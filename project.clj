(defproject omakase "0.0.1-SNAPSHOT"
  :description  "Start a cloud, manage it, and submit stuff."
  :url          "http://github.com/qadium/omakase"
  :license      {:name "Eclipse Public License"
                 :url "http://www.eclipse.org/legal/epl-v10.html"}
;  :bin          {:name "ok"
;                 :bootclasspath true}
  :dependencies [[org.clojure/clojure "1.6.0"]            ; it's clojure!
                 [org.clojure/tools.cli "0.3.1"]          ; for cli parsing
                 [org.clojure/data.json "0.2.5"]          ; for parsing json http get responses
                 [clj-ssh "0.5.11"]
;                 [org.clojure/core.async  "0.1.346.0-17112a-alpha"]
                 [com.climate/java.shell2 "0.1.0"]
;                 [com.palletops/pallet "0.8.0-RC.11"]
;                 [com.palletops/pallet-vmfest "0.4.0-alpha.1"]
;                 [org.clojars.tbatchelli/vboxjxpcom "4.3.4"]
;                 [com.palletops/pallet-aws "0.2.5"]
;                 [org.slf4j/jcl-over-slf4j "1.7.5"]
;                 [com.palletops/pallet-docker "0.1.0"]
                 [clj-http "1.0.1"]
  ; begin deps for jclouds
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.incubator "0.1.3"]
                 [org.apache.jclouds.driver/jclouds-slf4j "2.0.0-SNAPSHOT" ]
                 [ch.qos.logback/logback-classic "1.1.2"]
                 ;[org.apache.jclouds.labs/docker "2.0.0-SNAPSHOT" :exclusions [net.schmizz/sshj]]
                 [org.apache.jclouds/jclouds-all "2.0.0-SNAPSHOT"]
                 [org.apache.jclouds.driver/jclouds-log4j "2.0.0-SNAPSHOT"]
                 [org.apache.jclouds.driver/jclouds-sshj "2.0.0-SNAPSHOT" :exclusions [net.schmizz/sshj]]]
  :repositories {"jclouds-snapshot" "https://repository.apache.org/content/repositories/snapshots"}
  :main         ^:skip-aot omakase.core
  :target-path  "target/%s"
  :profiles     {:uberjar {:aot :all}})
