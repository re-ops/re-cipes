(ns re-cipes.docker.grafana
  "Dockerized Elastisearch only"
  (:require
   [re-cipes.hardening]
   [re-cipes.docker.nginx]
   [re-cipes.docker.server]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (copy directory chmod)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline volume
  "Setting up data volume"
  []
  (set-file-acl "re-ops" "rwX" "/var/")
  (directory "/var/data" :present)
  (chmod "/var/data" "a+wrx" {:recursive true}))

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.docker.grafana/volume]} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/compose/grafana"]
    (clone repo dest)
    (copy (<< "~{dest}/grafana.yml") (<< "~{dest}/docker-compose.yml"))
    (on-boot "docker-compose@grafana" :enable)))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]} nginx
  "Enabling site"
  []
  (let [external-port 3001
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "grafana" external-port 3000 false)
    (add-rule external-port :allow {})))
