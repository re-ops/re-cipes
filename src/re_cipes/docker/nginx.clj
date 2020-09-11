(ns re-cipes.docker.nginx
  "Dockerized nginx revese proxy support"
  (:require
   [re-cipes.docker.server]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.openssl :refer (generate-cert)]
   [re-cog.facts.datalog :refer (fqdn)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.file :refer (directory)]))

(require-recipe)

(def-inline {:depends #'re-cipes.docker.server/services} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/narkisr/nginx-proxy.git"
        dest "/etc/docker/compose/nginx-proxy"]
    (clone repo dest)
    (directory (<< "/etc/docker/compose/nginx-proxy/sites-enabled") :present)
    (on-boot "docker-compose@nginx-proxy" :enable)))

(def-inline {:depends #'re-cipes.docker.nginx/get-source} cert-generation
  "Generating ssl certs"
  []
  (let [host (fqdn) dest (<< "/etc/docker/compose/nginx-proxy/certs")]
    (directory dest :present)
    (generate-cert host dest)))

