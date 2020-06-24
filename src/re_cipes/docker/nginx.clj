(ns re-cipes.docker.nginx
  "Dockerized nginx revese proxy support"
  (:require
   [re-cipes.docker.server]
   [re-cipes.hardening]
   [re-cog.facts.datalog :refer (fqdn)]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.ufw :refer (set-state add-rule reset)]
   [re-cog.resources.file :refer (line line-set template)]))

(require-recipe)

(def-inline {:depends #'re-cipes.docker.server/services} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/narkisr/nginx-proxy.git"
        dest "/etc/docker/compose/nginx-proxy"]
    (clone repo dest)))

(def-inline {:depends #'re-cipes.docker.nginx/get-source} cert-generation
  "Generating ssl certs"
  []
  (let [openssl "/usr/bin/openssl"]
    (letfn [(certs [host dest]
              (fn []
                (let [subj (<< "'/C=pp/ST=pp/L=pp/O=pp Inc/OU=DevOps/CN=~{host}/emailAddress=dev@~{host}'")
                      keyout (<< "~{dest}/~{host}.key")
                      crt (<< "~{dest}/~{host}.crt")]
                  (script
                   (~openssl "req" "-x509" "-nodes" "-days" "365" "-newkey" "rsa:2048" "-keyout" ~keyout "-out" ~crt "-subj" ~subj)))))
            (dht [dest]
                 (fn []
                   (let [pem (<< "~{dest}/dhparam.pem")]
                     (script
                      (~openssl "dhparam" "-dsaparam" "-out" ~pem "4096")))))]
      (let [host (fqdn)
            dest (<< "/etc/docker/compose/nginx-proxy/certs")]
        (package "openssl" :present)
        (directory dest :present)
        (run (certs host dest))
        (run (dht dest))))))

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
               (set! H @("/usr/bin/openssl" "passwd" "-crypt" ~pass))
               (set! U ~user)
               ("/usr/bin/printf" (quoted "$H:$U\n") ">>" ~dest))))]
    (let [{:keys [nginx]} (configuration)
          {:keys [user pass]}  (nginx :htpasswd)
          dest "/etc/docker/compose/nginx-proxy/htpasswd"]
      (directory dest :present)
      (run (hash- user pass (<< "~{dest}/~(fqdn)"))))))
