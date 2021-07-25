(ns re-cipes.networking.nebula
  "Nebula setup"
  (:require
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.facts.datalog :refer (hostname)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.systemd :refer (set-service)]
   [re-cog.resources.file :refer (symlink directory template)]
   [re-cog.resources.archive :refer (untar)]))

(require-recipe)

(def-inline setup
  "Installing nebula binary"
  []
  (let [{:keys [home]} (configuration)
        version "v1.4.0"
        archive "nebula-linux-amd64.tar.gz"
        tmp (<< "/tmp/~{archive}")
        expected "d1ef37ca4d676f00df0ec83911cc2d9f1e70edc70651589210f9e97c68891b9b"
        url (<< "https://github.com/slackhq/nebula/releases/download/~{version}/~{archive}")]
    (download url tmp expected)
    (directory "/opt/nebula/" :present)
    (untar tmp "/opt/nebula/")
    (symlink (<< "/usr/local/bin/nebula") (<< "/opt/nebula/nebula"))))

(def-inline config
  "Nebula configuration"
  []
  (let [{:keys [hosts port]} (configuration :nebula)
        hosts' (into {}
                     (map (fn [[k {:keys [lighthouse?] :as m}]]
                            (if lighthouse?
                              [k (assoc m :port port :tun-disable true)]
                              [k (assoc m :port port :tun-disable false)])) hosts))
        lighthouse (first (filter :lighthouse? (vals hosts')))
        host (hosts' (hostname))
        args {:lighthouse lighthouse :host host :hostname (hostname)}]
    (directory "/etc/nebula/" :present)
    (template "/tmp/resources/templates/nebula/config.yml.mustache" "/etc/nebula/config.yml" args)))

(def-inline service
  "Setting up nebula service"
  []
  (let [opts {:wants "basic.target"
              :after "basic.target network.target"
              :restart "always"
              :stop "/bin/kill -HUP $MAINPID"
              :wanted-by "multi-user.target"}]
    (set-service "nebula" "Nebula Mesh VPN" "/usr/local/bin/nebula -config /etc/nebula/config.yml" opts)))
