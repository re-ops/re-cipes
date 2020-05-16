(ns re-cipes.elastic
  "ns setting up Elastisearch"
  (:require
   [re-cog.resources.exec :refer [run]]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.facts.datalog :refer (hostname fqdn)]
   [re-cipes.access :as access]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.file :refer (line template symlink directory)]
   [re-cog.resources.service :refer (service)]
   [re-cog.resources.package :refer (package key-file update- repository)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'access/repo} repo
  "Elastic repository setup"
  []
  (let [keyrings "/usr/share/keyrings"
        repo "deb https://artifacts.elastic.co/packages/6.x/apt stable main"
        key "GPG-KEY-elasticsearch"
        url (<< "https://artifacts.elastic.co/~{key}")]
    (download url (<< "~{keyrings}/~{key}") "10e406ba504706f44fbfa57a8daba5cec2678b31c1722e262ebecb5102d07659")
    (key-file (<< "~{keyrings}/~{key}"))
    (repository repo :present)
    (update-)))

(def-inline {:depends #'re-cipes.elastic/repo} elasticsearch-install
  "Setting up Elasticsearch"
  []
  (let [{:keys [cluster node data] :or {data "/var/lib/elasticsearch"}} (configuration :elastic :cluster)]
    (package "elasticsearch" :present)
    (set-file-acl "re-ops" "rwX" "/etc/elasticsearch")
    (line "/etc/elasticsearch/elasticsearch.yml" "network.host: 0.0.0.0\n" :present)
    (line "/etc/elasticsearch/elasticsearch.yml" (<< "cluster.name: ~{cluster}\n") :present)
    (line "/etc/elasticsearch/elasticsearch.yml" (<< "node.name: ~{node}") :present)
    ;; TODO (line "/etc/elasticsearch/elasticsearch.yml" "path.data:" data " " :set)
    (service "elasticsearch" :restart)))

(def-inline nginx
  "Setting up nginx server"
  []
  (package "nginx-light" :present))

(def-inline {:depends #'re-cipes.elastic/nginx} ssl-certificate
  "SSL certificates"
  []
  (letfn [(create-pem [dest]
            (fn []
              (let [pem (<< "~{dest}/dhparam.pem")]
                (script
                 ("/usr/bin/openssl" "dhparam" "-dsaparam" "-out" ~pem "4096")))))
          (create-cert [dest subj]
                       (fn []
                         (let [pem (<< "~{dest}/dhparam.pem")
                               key (<< "~{dest}/~(fqdn).key")
                               crt (<< "~{dest}/~(fqdn).crt")]
                           (script
                            ("/usr/bin/openssl" "req" "-x509" "-nodes" "-days" "365" "-newkey" "rsa:2048" "-keyout" ~key "-out" ~crt "-subj" ~subj)))))]
    (let [subj (<< "'/C=pp/ST=pp/L=pp/O=pp Inc/OU=DevOps/CN=~(fqdn)/emailAddress=dev@~(fqdn)'")
          dest "/etc/nginx/ssl"]
      (package "apache2-utils" :present)
      (package "openssl" :present)
      (set-file-acl "re-ops" "rwX" "/etc/nginx")
      (directory dest :present)
      (run (create-cert dest subj))
      (run (create-pem dest)))))

(def-inline {:depends [#'re-cipes.elastic/elasticsearch-install #'re-cipes.elastic/ssl-certificate]} elasticsearch-reverse-proxy
  "Nginx revese proxy for Elasticsearch"
  []
  (let [args (merge {:fqdn (fqdn) :product "elasticsearch"} (configuration :elastic :nginx :elasticsearch))]
    (set-file-acl "re-ops" "rwX" "/etc/nginx")
    (template "/tmp/resources/elastic/nginx/elk.mustache" "/etc/nginx/sites-available/elasticsearch.conf" args)
    (symlink "/etc/nginx/sites-enabled/elasticsearch.conf" "/etc/nginx/sites-available/elasticsearch.conf")))
