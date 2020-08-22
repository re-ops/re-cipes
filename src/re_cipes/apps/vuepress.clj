(ns re-cipes.apps.vuepress
  "Setting up vuepress"
  (:require
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.file :refer (symlink directory)]))

(require-recipe)

(def-inline node
  "Setting up nodejs"
  []
  (package "nodejs" :present)
  (package "npm" :present))

(def-inline {:depends #'re-cipes.apps.vuepress/node} vuepress
  "Setting up vuepress"
  []
  (letfn [(install []
            (script ("/usr/bin/npm" "install" "vuepress")))]
    (let [{:keys [home]} (configuration)]
      (run install)
      (directory (<< "~{home}/bin/") :present)
      (symlink (<< "~{home}/bin/vuepress") (<< "~{home}/node_modules/vuepress/cli.js")))))
