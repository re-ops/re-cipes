(ns re-cipes.re-ops.re-gent
  "Setting up re-gent support"
  (:require
   [re-cog.resources.file :refer (template directory edn-set chown chmod)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.systemd :refer (set-service)]))

(require-recipe)

(def-inline service
  "Setting up a user service for re-gent"
  []
  (let [{:keys [home user]} (configuration)
        config {:user user :environment-file (<< "~{home}/.re-gent.env") :restart true}]
    (set-service "re-gent" "Re-gent user service" (<< "~{home}/re-gent $SERVER $PORT $LOG") config)))
