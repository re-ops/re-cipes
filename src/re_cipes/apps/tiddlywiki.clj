(ns re-cipes.apps.tiddlywiki
  (:require
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.openssl :refer (generate-cert)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.file :refer (symlink directory)]))

(require-recipe)

(def-inline node
  "Setting up nodejs"
  []
  (package "nodejs" :present)
  (package "npm" :present))

(def-inline {:depends #'re-cipes.apps.tiddlywiki/node} tiddlywiki
  "Setting up tiddlywiki"
  []
  (letfn [(install []
            (script ("/usr/bin/npm" "install" "tiddlywiki")))]
    (let [{:keys [home]} (configuration)
          dest (<< "~{home}/certs")]
      (run install)
      (directory (<< "~{home}/bin/") :present)
      (directory dest :present)
      (generate-cert (fqdn) dest)
      (symlink (<< "~{home}/bin/tiddlywiki") (<< "~{home}/node_modules/tiddlywiki/tiddlywiki.js")))))
