(ns re-cipes.clojure
  "Setting up Clojure tooling"
  (:require
   [re-cipes.shell :refer (dot-files)]
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.archive :refer (unzip)]
   [re-cog.resources.file :refer (symlink directory chmod chown)]))

(require-recipe)

(def-inline {:depends #'re-cipes.shell/dot-files} clj
  "Setting up clj and deps tools"
  []
  (letfn [(install-fn [ins prefix]
            (fn []
              (script (~ins "--prefix" ~prefix))))]
    (let [{:keys [home user]} (configuration)
          ins "linux-install-1.10.1.469.sh"
          url (<< "https://download.clojure.org/install/~{ins}")
          sum "265e46d492b682cecc346ef018076a3181203dfe5e3dbe455b0f7feab51df70f"
          prefix (<< "~{home}/.clojure")]
      (download url (<< "/tmp/~{ins}") sum)
      (package "curl" :present)
      (package "rlwrap" :present)
      (chmod (<< "/tmp/~{ins}") "+x" {})
      (run (install-fn (<< "/tmp/~{ins}") prefix))
      (directory (<< "~{home}/bin/") :present)
      (symlink (<< "~{home}/bin/clj") (<< "~{prefix}/bin/clj"))
      (symlink (<< "~{home}/bin/clojure") (<< "~{prefix}/bin/clojure"))
      (symlink (<< "~{prefix}/deps.edn") (<< "~{home}/.dots/deps.edn"))
      (chown prefix user user {:recursive true}))))

(def-inline {:depends #'re-cipes.shell/dot-files} lein
  "Setting up https://leiningen.org/"
  []
  (let [{:keys [home user]} (configuration)
        dest (<< "~{home}/bin/lein")
        sum "f6312bd74463b2998eeeb1be823c8ddf22f54ce1b4d5f33a9578155852b89122"
        url "https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein"
        dot-lein (<< "~{home}/.lein")]
    (directory (<< "~{home}/bin") :present)
    (download url dest sum)
    (chmod dest "+x" {})
    (directory dot-lein :present)
    (symlink (<< "~{dot-lein}/profiles.clj") (<< "~{home}/.dots/profiles.clj"))))

(def-inline joker
  "Setting up Joker linter"
  []
  (let [{:keys [home]} (configuration)
        version "0.12.7"
        archive (<< "joker-~{version}-linux-amd64.zip")
        url (<< "https://github.com/candid82/joker/releases/download/v~{version}/~{archive}")
        sum "25ba334d68044971e556e9aa0ce6c1994610a464c399adf0e0357fd2e23b6c36"]
    (download url (<< "/tmp/~{archive}") sum)
    (directory (<< "~{home}/bin/") :present)
    (unzip (<< "/tmp/~{archive}") (<< "~{home}/bin/"))))

(def-inline cljfmt
  "Single binary code format for Clojure"
  []
  (let [{:keys [home user]} (configuration)
        url "https://github.com/narkisr/cljfmt-graalvm/releases/download/0.1.0/cljfmt"
        dest (<< "~{home}/bin/cljfmt")]
    (directory (<< "~{home}/bin") :present)
    (download url dest "290872ee18769995b3a2e8e5b12711586fdfcf5dca26b78b79b87d8fc8eab495")
    (chmod dest "+x" {})))
