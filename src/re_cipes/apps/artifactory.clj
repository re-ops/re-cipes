(ns re-cipes.apps.artifactory
  "Dockerized Artifactory instance"
  (:require
   [re-cipes.hardening]
   [re-cipes.docker.nginx]
   [re-cipes.docker.server]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (copy directory chmod)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline volume
  "Setting up data volume"
  []
  (set-file-acl "re-ops" "rwx" "/var/local/")
  (directory "/var/local/data" :present)
  (chmod "/var/local/data" "a+wrx" {:recursive true}))

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.apps.artifactory/volume]} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/compose/artifactory"]
    (clone repo dest {})
    (copy (<< "~{dest}/artifactory.yml") (<< "~{dest}/docker-compose.yml"))
    (on-boot "docker-compose@artifactory" :enable)))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]} nginx
  "Enabling site"
  []
  (let [external-port 8443
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "artifactory" external-port 8081 false)
    (add-rule external-port :allow {})))
