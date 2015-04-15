(ns omakase.prompt)

(defn ask
  "Ask the user for some input"
  [prompt]
  (print (format "%s " prompt))
  (flush)
  (read-line))
