(ns re-cipes.security.beats
  "Setting up beats agent for log forwarding"
  (:require
   re-cipes.access
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.file :refer (yaml-set file)]
   [re-cog.resources.package :refer (package update- key-file)]
   [re-cog.resources.permissions :refer (set-file-acl)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} install
  "Install beats"
  []
  (let [repo "deb https://packages.elastic.co/beats/apt stable main"
        sources "/etc/apt/sources.list.d"
        listing (<< "~{sources}/beats.list")
        keyrings "/usr/share/keyrings"
        key "GPG-KEY-elasticsearch"
        url (<< "https://packages.elastic.co/~{key}")]
    (download url (<< "~{keyrings}/~{key}") "10e406ba504706f44fbfa57a8daba5cec2678b31c1722e262ebecb5102d07659")
    (key-file (<< "~{keyrings}/~{key}"))
    (file listing :present)
    (line listing repo :present)
    (update-)
    (package "filebeat" :present)))

(def-inline {:depends [#'re-cipes.security.beats/install #'re-cipes.access/permissions]} configure
  "Configure beats over ssl"
  []
  (let [parent "/etc/filebeat/"
        dest (<< "~{parent}/filebeat.yml")
        {:keys [hosts]} (configuration :logstash :server)]
    (set-file-acl "re-ops" "rwx" "/etc/filebeat")
    (yaml-set dest [:output] {})
    (yaml-set dest [:output :logstash] {})
    (yaml-set dest [:output :logstash :hosts] hosts)
    (yaml-set dest [:output :logstash :tls :insecure] true)
    (yaml-set dest [:filebeat :prospectors 0 :paths] ["/var/log/*.log" "/var/log/syslog"])
    (service "filebeat" :restart)))
