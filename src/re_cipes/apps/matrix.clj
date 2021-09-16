(ns re-cipes.apps.matrix
  (:require
   [re-cipes.docker.nginx]
   [re-cipes.docker.re-dock]
   [re-cog.facts.datalog :refer (hostname fqdn)]
   [re-cog.resources.exec :refer [run]]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (copy symlink directory)]
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

(def-inline {:depends [#'re-cipes.apps.matrix/setup]} configure
  "Configuration"
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
      (run configure))))
