(defproject re-cipes "0.1.0"
  :description "Recipes for provioning Linux machines"
  :url "https://github.com/re-ops/re-cipes"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
      [org.clojure/clojure "1.10.1"]
      [re-cog "0.4.0"]
  ]

  :plugins [
      [lein-tag "0.1.0"]
      [lein-ancient "0.6.15" :exclusions [org.clojure/clojure]]
      [lein-set-version "0.3.0"]
      [lein-cljfmt "0.5.6"]]

  :aliases {
    "travis" [
       "do" "clean," "compile," "cljfmt" "check"
     ]
  }
  :repl-options {:init-ns re-cipes.core})
