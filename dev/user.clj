(ns user
  (:require
   [re-share.log :refer (debug-on debug-off setup)]
   [clojure.repl :refer :all]
   [re-cog.facts.datalog :refer (populate)]
   [clojure.tools.namespace.repl :refer (refresh refresh-all)]))

(defn init
  "Constructs the current development system."
  []
  (setup "re-cipes" ["oshi.*"] [])
  (populate))

(defn stop
  "Shuts down and destroys the current development system."
  [])

(defn go
  "Initializes the current development system and starts it running."
  []
  (init))

(defn reset []
  (stop)
  (refresh :after 'user/go))

(defn clrs
  "clean repl"
  []
  (print (str (char 27) "[2J")))
