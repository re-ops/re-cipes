(ns re-cipes.apps.matrix
  (:require
   [re-cipes.docker.nginx]
   [re-cipes.docker.re-dock]
   [re-cog.facts.datalog :refer (hostname fqdn)]
   [re-cog.resources.exec :refer [run]]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (copy symlink directory file line yaml-set chmod)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.docker.re-dock/volume #'re-cipes.docker.re-dock/repo]}
  setup
  "setup Matrix"
  []
  (let [repo "/etc/docker/re-dock/matrix"
        dest "/etc/docker/compose/matrix"
        {:keys [password]} (configuration :matrix :postgres)]
    (copy (<< "~{repo}/matrix.yml") (<< "~{repo}/docker-compose.yml"))
    (symlink dest repo)
    (on-boot "docker-compose@matrix" :enable)))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall #'re-cipes.apps.matrix/setup]}
  element
  "Element"
  []
  (let [external-port 8443
        file "config.json"
        dest "/etc/docker/compose/matrix"
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "element" external-port 8080 {})
    (add-rule external-port :allow {})
    (template (<< "/tmp/resources/templates/matrix/element/~{file}.mustache") (<< "~{dest}/~{file}") {:hostname (hostname) :fqdn (fqdn)})))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]} synapse-proxy
  "Synapse proxy"
  []
  (let [external-port 443
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "synapse" external-port 8008 {:http-2 true})
    (add-rule external-port :allow {})))

(def-inline {:depends [#'re-cipes.apps.matrix/setup]} generate
  "Generate configuration"
  []
  (let [host (fqdn)
        parent "/etc/docker/compose/matrix/matrix"
        dest (<< "~{parent}/synapse")
        config (<< "~{dest}/homeserver.yaml")]
    (letfn [(pip []
              (script
               ("/usr/bin/pip3" "install" "matrix-synapse")))
            (configure []
                       (script ("/usr/bin/python3" "-m" "synapse.app.homeserver" "--server-name" ~host
                                                   "--config-path" ~config "--config-directory" ~dest "--generate-config" "--report-stats=no")))]
      (package "python3-pip" :present)
      (directory parent :present)
      (directory dest :present)
      (run pip)
      (file config :absent)
      (run configure)
      (set-file-acl "re-ops" "rw" dest)
      (chmod dest "o+rw" {:recursive true}))))

(def-inline {:depends [#'re-cipes.apps.matrix/setup]} env
  "Docker env file"
  []
  (let [{:keys [password]} (configuration :matrix :postgres)
        parent "/etc/docker/compose/matrix/"
        env (<< "~{parent}/.env")]
    (file env :present)
    (line env (<< "POSTGRES_PASSWORD=~{password}") :present)))

(def-inline {:depends [#'re-cipes.apps.matrix/generate]} configure
  "General Configuration"
  []
  (let [{:keys [password]} (configuration :matrix :postgres)
        dest "/etc/docker/compose/matrix/matrix/synapse"
        homeserver (<< "~{dest}/homeserver.yaml")]
    (set-file-acl "re-ops" "rw" dest)
    (chmod dest "o+rw" {:recursive true})
    (yaml-set (<< "~{dest}/~(hostname).log.config") [:handlers :file :filename] (<< "/data/homeserver.log"))
    (yaml-set homeserver [:log_config] (<< "/data/~(hostname).log.config"))
    (yaml-set homeserver [:signing_key_path] (<< "/data/~(hostname).signing.key"))
    (yaml-set homeserver [:media_store_path] (<< "/data/media_store"))
    (yaml-set homeserver [:pid_file] (<< "/data/homeserver.pid"))
    (yaml-set homeserver [:listeners 0 :bind_addresses] ["0.0.0.0"])))

(def-inline {:depends [#'re-cipes.apps.matrix/generate]} database
  "DB Configuration"
  []
  (let [{:keys [password]} (configuration :matrix :postgres)
        dest "/etc/docker/compose/matrix/matrix/synapse"
        homeserver (<< "~{dest}/homeserver.yaml")]
    (yaml-set homeserver [:database :name] "psycopg2")
    (yaml-set homeserver [:database :txn_limit] 10000)
    (yaml-set homeserver [:database :args :user] "synapse_user")
    (yaml-set homeserver [:database :args :password] password)
    (yaml-set homeserver [:database :args :database] "synapse")
    (yaml-set homeserver [:database :args :host] "postgres")
    (yaml-set homeserver [:database :args :port] 5432)
    (yaml-set homeserver [:database :args :cp_min] 5)
    (yaml-set homeserver [:database :args :cp_max] 10)))
