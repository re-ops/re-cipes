(ns re-cipes.ops
  "Setting up Re-ops"
  (:require
   [re-cog.resources.git :refer (clone)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.file :refer (directory copy)]))

(require-recipe)

(def-inline repositories
  "Setting up Re-ops repositories"
  []
  (let [{:keys [home user]} (configuration)
        root (<< "~{home}/re-ops")
        repos ["re-core" "re-pack" "re-dock" "re-cipes" "re-gent"]]
    (directory root :present)
    (doseq [repo repos]
      (let [dest (<< "~{root}/~{repo}")]
        (clone (<< "git://github.com/re-ops/~{repo}.git") dest)))))

(def-inline {:depends #'re-cipes.ops/repositories} configure
  "Set basic Re-ops configuraion files"
  []
  (let [{:keys [home]} (configuration)]
    (copy (<< "~{home}/re-ops/re-core/resources/re-ops.edn") (<< "~{home}/.re-ops.edn"))
    (copy (<< "~{home}/re-ops/re-core/resources/secrets.edn") "/tmp/secrets.edn")))
