(ns omakase.utils
  (:require [omakase.io :as okio]
            [omakase.tunnel :as tunnel]
            [clj-http.client :as http]
            [clojure.tools.cli :as cli]
            [clojure.stacktrace :as trace]
            [clojure.data.json :as json]
            [clojure.string :as string]))

(defn ^:private arg-dispatch
  [& args]
  (keyword (first args)))

(defn show-msg
  "Displays a message to stdout and returns a given status."
  ([msg]
   (show-msg msg :app/ok))
  ([msg status]
   (println msg)
   status))

(defn cluster-ip
  "Gets an IP address for the cluster."
  [cluster-name]
  (string/trim-newline (slurp (okio/cluster-file cluster-name "ip"))))

(defn ^:private get-etcd-keys
  [cluster-name etcd-key]
  (tunnel/with-ssh-tunnel cluster-name (cluster-ip cluster-name) 4001 4001
    (let [req   (http/get (str "http://127.0.0.1:4001/v2/keys" etcd-key)
                          {:query-params {"recursive" "false" "sorted" "false"}})
          data  (json/read-str (req :body) :key-fn keyword)]
      (:value (:node data)))))

(defn cluster-kafka-ip
  "Gets the public ip for kafka."
  [cluster-name]
  (get-etcd-keys cluster-name "/public/kafka"))

(defn cluster-zk-ip
  "Gets the public ip for zookeeper."
  [cluster-name]
  (get-etcd-keys cluster-name "/public/zookeeper"))

(defn wordwrap
  "Wraps a string of text after n characters. Respects any newlines in the string."
  [string n]
  (let [re (re-pattern (format ".{0,%d}[ \n]" n))]
    (->> (str string " ")
         (re-seq re)
         (map string/trimr)
         (string/join \newline))))

(defn usage [command description required-args summary]
  (->> [(str "Usage: ok " command " " required-args)
        ""
        (wordwrap description 72)
        ""
        "Options:"
        ""
        summary]
       (string/join \newline)))

(defn patch-msg
  "Will take error messages of the form
     'Wrong number of args (2) passed to: core/eval12927/joe--12928'
   and patch it to
     'Wrong number of args (2) passed to the comand 'joe'. Expected [x]'"
  [msg func-name func-arglist]
  (let [arg-options   (string/join ", or " func-arglist)
        replace-str   (str " the command '" func-name "'. Expected " arg-options ".")]
    (string/replace msg #": [\w\d\/-]+$" replace-str)))

(defmulti short-help-command
  "A multimethod to get help on commands."
  arg-dispatch)

(defmethod short-help-command :default
  [_]
  (show-msg "No help available."))

(defmulti run-command
  "A multitmethod to dispatch commands."
  {:arglists '([& args])}
  arg-dispatch)

(defmethod run-command :default
  [& args]
  (if (seq args)
    (show-msg (str "Unknown command: " (first args)) :app/fail)
    (show-msg "No command provided." :app/fail)))

(defmacro arity-checked
  "Wraps the function to check its arity. Returns the result of the function
   or :app/fail if an exception occurred.

   Must be a macro to do runtime reflection on the function."
  [func]
  `(let [{arglist# :arglists, name# :name} (meta (var ~func))]
     (fn [& args#]
       (try
         (apply ~func args#)
         (catch clojure.lang.ArityException e#
           (show-msg (patch-msg (.getMessage e#) name# arglist#) :app/fail))
         (catch Exception e#
           (trace/print-cause-trace e#)
           (show-msg (.getMessage e#) :app/fail))))))

(defn execute-command
  "Executes the command and displays help text if any exceptions occur."
  [command options arguments help-text user-opts]
  (let [func-apply  (if user-opts
                      (apply command [options] arguments)
                      (apply command arguments))]
    (case func-apply
      :app/fail (show-msg help-text :app/fail)
      (show-msg "Ok, goodbye!"))))

(defmacro defcommand
  "This macro defines a function that can be used as a command along with
   its help data.

   It adds to the run-command and short-help-command multimethods.

   It also defn's the function."
  [command-name docstring & body]
  `(let [command# (keyword (quote ~command-name))]
     ;; define the function in the macro so that its metadata is available
     ;; at runtime, this is done in the parent scope, so
     ;;     (defcommand foobar ...)
     ;; makes available a function (foobar ...) in the namespace

     (defn ~command-name
       ~docstring
       ~@body)

     (let [{arglist#      :arglists
            command-opts# :options
            description#  :description
            in-order#     :in-order}      (meta (var ~command-name))
           options#                       (conj command-opts# ["-h" "--help"])
           required-args#                 (string/join " | " arglist#)]
       (defmethod run-command command#
         [& args#]
         (let [{arguments#  :arguments
                errors#     :errors
                opts#       :options
                summary#    :summary} (cli/parse-opts args# options# :in-order in-order#)
               help-text#              (usage '~command-name (or description# ~docstring) required-args# summary#)
               checked-cmd#            (arity-checked ~command-name)]
           (cond
             (:help opts#)  (show-msg help-text#)
             errors#        (show-msg (string/join \newline errors#) :app/fail)
             :else          (execute-command checked-cmd# opts# (rest arguments#) help-text# command-opts#)))))

     (defmethod short-help-command command#
       [_#]
       (format "%-10s %s" (quote ~command-name) ~docstring))))
