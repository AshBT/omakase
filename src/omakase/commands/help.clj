(ns omakase.commands.help
  (:require [omakase.utils :as utils]))

(utils/defcommand help
  "Show help for a command."
  {:description "The help command simply forwards the '-h' flag to the command for which help is requested."}
  [command]
  (utils/run-command command "-h"))
