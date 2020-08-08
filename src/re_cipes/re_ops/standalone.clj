(ns re-cipes.re-ops.standalone
  (:require
   [re-cipes.re-ops.core]
   [re-cog.resources.git :refer (clone)]
   [re-cog.resources.file :refer (edn-set line)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]))

(require-recipe)

(def-inline {:depends #'re-cipes.re-ops.core/configure} lxc-pass
  "Set lxc auth configuration"
  []
  (let [{:keys [home lxd]} (configuration)
        file (<< "~{home}/.re-ops.edn")
        http "        :hosts [\"http://localhost:9200\"]"
        https "        :hosts [\"https://localhost:9200\"]"]
    (edn-set "/tmp/secrets.edn" [:lxc :pass] (lxd :password))
    (line file https :replace :with http)))
