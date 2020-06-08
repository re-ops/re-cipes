(defproject re-cipes "0.1.5"
  :description "Recipes for provioning Linux machines"
  :url "https://github.com/re-ops/re-cipes"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
      [org.clojure/clojure "1.10.1"]
      [re-cog "0.4.10"]
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

  :profiles {
     :dev {
       :source-paths  ["dev"]
       :set-version {
         :updates [
            {:path "main/re_cipes/main.clj" :search-regex #"\"\d+\.\d+\.\d+\""}
            {:path "bin/binary.sh" :search-regex #"\d+\.\d+\.\d+"}
            {:path "README.md" :search-regex #"\d+\.\d+\.\d+"}
          ]}

     }

    :package {
        :source-paths  ["src" "main"]
        :main re-cipes.main
    }
  }

  :repl-options {
    :init-ns user
    :prompt (fn [ns] (str "\u001B[35m[\u001B[34m" "re-cipes" "\u001B[35m]\u001B[33mλ:\u001B[m " ))
    :welcome (println "Welcome to re-cipes!" )
  }


)
