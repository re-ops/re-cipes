(defproject re-cipes "0.1.0"
  :description "Recipes for provioning (mainly) Ubuntu machines"
  :url "https://github.com/re-ops/re-cipes"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
      [org.clojure/clojure "1.10.0"]
      [re-cog "0.4.0"]
  ]
  :repl-options {:init-ns re-cipes.core})
