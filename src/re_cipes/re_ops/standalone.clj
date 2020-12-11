(ns re-cipes.re-ops.standalone
  (:require
   [re-cipes.re-ops.core]
   [re-cog.resources.file :refer (edn-set line chown copy)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]))

(require-recipe)

(def-inline {:depends #'re-cipes.re-ops.core/configure} elastic-http
  "Set elasticseach to plain http"
  []
  (let [{:keys [home lxd user]} (configuration)
        file (<< "~{home}/.re-ops.edn")
        http  "        :hosts [\"http://localhost:9200\"]"
        https "        :hosts [\"https://localhost:9200\"]"]
    (line file https :replace :with http)))
