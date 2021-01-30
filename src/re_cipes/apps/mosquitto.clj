(ns re-cipes.apps.mosquitto
  (:require
   [re-cipes.docker.server]
   [re-cipes.hardening]
   [re-cog.resources.file :refer (template)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.hardening/firewall]}
  service
  "Setup mosquitto docker service"
  []
  (let [args {:description "Mosquitto Container" :port 1883 :image "eclipse-mosquitto"}
        source "/tmp/resources/templates/docker/docker.service.mustache"
        dest "/etc/systemd/system/docker-mosquitto.service"]
    (template source dest args)
    (on-boot "docker-mosquitto" :enable)))
