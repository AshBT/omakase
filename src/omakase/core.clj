(ns omakase.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [omakase.utils :as utils]
            omakase.commands.version
            omakase.commands.help
            omakase.commands.start
            omakase.commands.status
            omakase.commands.launch
            omakase.commands.land
            omakase.commands.fleet
            omakase.commands.etcdctl
            omakase.commands.ssh
            omakase.commands.run
            omakase.commands.config
            omakase.commands.create)
            ; require the commands so their multimethods get registered
  (:gen-class))

(def cli-options
  [["-h" "--help"]
   [nil  "--version" "version information for omakase"]
   ["-l" "--log-level INFO" "level of logging"]])

  ;; to implement:
  ;;    activate? -- activate some cloud
  ;;
  ;;    status  -- status of cloud?
  ;;    clouds  -- list cloud names? (or does "list" do this?)
  ;;    connect -- connect to existing cloud provider
  ;;    dist    -- package up a topo
  ;;    deploy  -- deploy a topo
  ;;    open    -- view the running app in web server
  ;;    clone   -- clone a service template from github
  ;;    run
  ;;    submit  -- package service into docker container and push to cloud
  ;;    remove  -- remove service
  ;;
  ;;    whoami  -- which cloud am i on?

(defn construct-subcommand-summary
  "Returns the help text for a group of subcommands, e.g., those related to a cluster."
  [label subcommand-set]
  (->> (map #(str "    " (utils/short-help-command %1)) subcommand-set)
       (cons label)
       (string/join \newline)))

(defn subcommand-summary
  "Summarizes all the subcommands"
  []
  (let [subcommands   (methods utils/run-command)
        implemented   (keys (dissoc subcommands :default))
        cluster-cmds  #{:create :launch :land}
        service-cmds  #{:start}
        utility-cmds  #{:fleet :ssh :etcdctl}
        general-cmds  (remove #(or (contains? cluster-cmds %1)
                                   (contains? service-cmds %1)
                                   (contains? utility-cmds %1)) implemented)
        cluster-help  (construct-subcommand-summary "  Clusters:" cluster-cmds)
        service-help  (construct-subcommand-summary "  Services:" service-cmds)
        utility-help  (construct-subcommand-summary "  Utilities:" utility-cmds)
        general-help  (construct-subcommand-summary "  General:" general-cmds)]
      (string/join "\n\n" [general-help utility-help cluster-help service-help])))

(defn usage [summary]
  (->> ["Usage: ok [OPTIONS] COMMAND [arg...]"
        ""
        "A tool for dynamic clouds."
        ""
        "Options:"
        ""
        summary
        ""
        "Commands:"
        (subcommand-summary)
        ""
        "Run 'ok COMMAND --help' for more information on a command."]
       (string/join \newline)))

(defn app
  "Runs the main app"
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options :in-order true)]
    (cond
      (:help options)     (utils/show-msg (usage summary))
      (:version options)  (utils/run-command "version")
      errors              (utils/show-msg (string/join \newline errors) :app/fail)
      :else               (apply utils/run-command arguments))))

(defn -main
  "Checks return status and exits with the proper return code."
  [& args]
  (System/exit
    (case (apply app args)
      :app/ok     0
      :app/fail   1
                  1)))

