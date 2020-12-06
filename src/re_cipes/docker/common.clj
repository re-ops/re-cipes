(ns re-cipes.docker.common
  "Common docker elastic setup"
  (:require
   [re-cipes.docker.server]
   [re-cipes.access]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.file :refer (directory chmod chown line file)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline volume
  "Setting up data volume"
  []
  (let [{:keys [user]} (configuration)]
    (set-file-acl "re-ops" "rwX" "/var/")
    (directory "/var/data" :present)
    (directory "/var/data/elasticsearch" :present)
    (directory "/var/data/grafana" :present)
    (chmod "/var/data/" "a+wrx" {:recursive true})
    (chown "/var/data/" user user {:recursive true})))

(def-inline {:depends [#'re-cipes.docker.server/install #'re-cipes.access/permissions]} re-dock
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/re-dock/"
        env (<< "~{dest}/.env")
        {:keys [password]} (configuration :elasticsearch)]
    (directory "/etc/docker/compose" :present)
    (clone repo dest)
    (file env :present)
    (line env (<< "ELASTIC_PASSWORD=~{password}") :present)))
