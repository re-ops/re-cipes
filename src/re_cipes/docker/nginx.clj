(ns re-cipes.docker.nginx
  "Dockerized nginx revese proxy support"
  (:require
   [re-cipes.docker.server]
   [re-cipes.hardening]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.openssl :refer (generate-cert)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.facts.datalog :refer (fqdn)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.ufw :refer (set-state add-rule reset)]
   [re-cog.resources.file :refer (line line-set template)]))

(require-recipe)

(def-inline {:depends #'re-cipes.docker.server/services} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/narkisr/nginx-proxy.git"
        dest "/etc/docker/compose/nginx-proxy"]
    (clone repo dest)
    (on-boot "docker-compose@nginx-proxy" :enable)))

(def-inline {:depends #'re-cipes.docker.nginx/get-source} cert-generation
  "Generating ssl certs"
  []
  (let [host (fqdn) dest (<< "/etc/docker/compose/nginx-proxy/certs")]
    (directory dest :present)
    (generate-cert host dest)))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]} enabled-sites
  "Configuration of nginx sites"
  []
  (let [{:keys [nginx]} (configuration)
        sites-enabled (<< "/etc/docker/compose/nginx-proxy/sites-enabled")]
    (directory sites-enabled :present)
    (doseq [{:keys [name external-port] :as m} (nginx :sites)
            :let [site (assoc m :fqdn (fqdn))]]
      (template "/tmp/resources/templates/nginx/site.conf" (<< "~{sites-enabled}/~{name}.conf") site)
      (add-rule external-port :allow {}))))

(def-inline {:depends #'re-cipes.docker.nginx/get-source} htpasswd
  "Creating htpasswd"
  []
  (letfn [(hash- [user pass dest]
            (fn []
              (script
               (set! H @(~openssl-bin "passwd" "-crypt" ~pass))
               (set! U ~user)
               ("/usr/bin/printf" (quoted "$U:$H\n") ">>" ~dest))))]
    (let [{:keys [nginx]} (configuration)
          {:keys [user pass]}  (nginx :htpasswd)
          dest "/etc/docker/compose/nginx-proxy/htpasswd"]
      (directory dest :present)
      (run (hash- user pass (<< "~{dest}/~(fqdn)"))))))
