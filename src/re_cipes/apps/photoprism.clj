(ns re-cipes.apps.photoprism
  "Dockerized photoprism see https://docs.photoprism.org/getting-started/docker-compose/"
  (:require
   [re-cipes.hardening]
   [re-cipes.docker.nginx]
   [re-cipes.docker.server]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (yaml-set directory)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.docker.server/services]}
  setup
  "Setup photoprism"
  []
  (let [parent "/etc/docker/compose/photoprism"
        dest (<< "~{parent}/docker-compose.yml")
        sum "59b8d528eaf021569b7a3ff3048b022dd3fd426e0ae1fa124e7e746831332fcf"
        url "https://dl.photoprism.org/docker/docker-compose.yml"
        {:keys [passwords volumes]} (configuration :photoprism)
        originals (mapv (fn [[k v]] (<< "~{v}:/photoprism/originals/~(name k)")) volumes)
        storage "./storage:/photoprism/storage"]
    (directory parent :present)
    (download url dest sum)
    (yaml-set dest [:services :photoprism :environment :PHOTOPRISM_ADMIN_PASSWORD] (passwords :admin))
    (yaml-set dest [:services :photoprism :environment :PHOTOPRISM_DATABASE_PASSWORD] (passwords :db))
    (yaml-set dest [:services :mariadb    :environment :MYSQL_ROOT_PASSWORD] (passwords :db))
    (yaml-set dest [:services :mariadb    :environment :MYSQL_PASSWORD] (passwords :db))
    (yaml-set dest [:services :photoprism :environment :PHOTOPRISM_DISABLE_WEBDAV] "true")
    (yaml-set dest [:services :photoprism :environment :PHOTOPRISM_DISABLE_SETTINGS] "true")
    (yaml-set dest [:services :photoprism :volumes] (conj originals storage))
    (on-boot "docker-compose@photoprism" :enable)))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]} nginx
  "Nginx site enable"
  []
  (let [external-port 443
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "photoprism" external-port 2342 {:websockets true})
    (add-rule external-port :allow {})))
