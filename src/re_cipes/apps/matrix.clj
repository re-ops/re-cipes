(ns re-cipes.apps.matrix
  (:require
   [re-cipes.docker.nginx]
   [re-cipes.docker.re-dock]
   [re-cog.facts.datalog :refer (hostname fqdn)]
   [re-cog.resources.exec :refer [run]]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (copy symlink)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.docker.re-dock/volume #'re-cipes.docker.re-dock/repo]}
  setup
  "setup Matrix"
  []
  (let [repo "/etc/docker/re-dock/matrix"
        dest "/etc/docker/compose/matrix"
        env (<< "~{dest}/.env")
        {:keys [password]} (configuration :matrix :postgres)]
    (copy (<< "~{repo}/matrix.yml") (<< "~{repo}/docker-compose.yml"))
    (symlink dest repo)
    (on-boot "docker-compose@matrix" :enable)))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]}
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
  "Config generation"
  []
  (letfn [(generate [volume host]
            (fn []
              (script ("/usr/bin/docker" "run" "-i" "--rm"
                                         "-v" ~volume
                                         "-e" ~host
                                         "-e" "SYNAPSE_REPORT_STATS=yes"
                                         "matrixdotorg/synapse:latest"
                                         "generate"))))]
    (let [volume "/etc/docker/compose/matrix/matrix/synapse:/data"
          host (<< "SYNAPSE_SERVER_NAME=~(fqdn)")]
      (run (generate volume host)))))
