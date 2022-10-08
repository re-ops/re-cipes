(ns re-cipes.apps.wallabag
  "Dockerized Wallabag"
  (:require
   [re-cipes.hardening]
   [re-cipes.docker.server]
   [re-cipes.docker.nginx]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (copy directory chmod line file)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline volume
  "Setting up data volume"
  []
  (set-file-acl "re-ops" "rwx" "/opt")
  (directory "/opt/wallabag" :present)
  (directory "/opt/wallabag/data" :present)
  (directory "/opt/wallabag/images" :present)
  (chmod "/opt/wallabag" "a+wrx" {:recursive true}))

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.apps.wallabag/volume]} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/compose/wallabag"
        {:keys [db_root_password db_password email domain name]} (configuration :wallabag)
        env (<< "~{dest}/.env")]
    (clone repo dest {})
    (copy (<< "~{dest}/apps/wallabag.yml") (<< "~{dest}/docker-compose.yml"))
    (file env :present)
    (line env (<< "DB_ROOT_PASSWORD=~{db_root_password}") :present)
    (line env (<< "DB_PASSWORD=~{db_password}") :present)
    (line env (<< "EMAIL=~{email}") :present)
    (line env (<< "DOMAIN=http://~{domain}:8080") :present)
    (line env (<< "NAME=~{name}") :present)
    (on-boot "docker-compose@wallabag" :enable)))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]} nginx
  "Enabling reverse proxy"
  []
  (let [external-port 443
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "wallabag" external-port 8080 {:tls-3 true})
    (add-rule external-port :allow {})))
