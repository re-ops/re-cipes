(ns re-cipes.profiles)

; Basic profiles

(def ^{:doc "Setting up access and hardening only"}
  lean #{'re-cipes.monitoring 're-cipes.access 're-cipes.hardening})

(def ^{:doc "A common base setup"}
  base (into #{'re-cipes.monitoring 're-cipes.security.beats 're-cipes.networking.nebula
               're-cipes.shell 're-cipes.tmux 're-cipes.desktop} lean))

(def ^{:doc "A secure Base setup including EDM"}
  base-edm (into #{'re-cipes.security.wazuh 're-cipes.security.osquery} base))

; Re-core

(def ^{:doc "Re-ops instance"}
  re-ops (into #{'re-cipes.re-ops.core 're-cipes.clojure 're-cipes.packer 're-cipes.nvim} base))

(def ^{:doc "Re-ops standlone development instance with Docker and LXC enabled"}
  re-ops-standalone (into #{'re-cipes.re-ops.standalone 're-cipes.infra.lxd 're-cipes.docker.server 're-cipes.apps.elasticsearch} re-ops))

(def ^{:doc "Re-ops packer image basic setup"}
  re-ops-image #{'re-cipes.hardening 're-cipes.re-ops.re-gent})

; Infra profiles

(def nas (into #{'re-cipes.backup 're-cipes.infra.zfs} lean))

(def wireguard (into #{'re-cipes.wireguard} lean))

(def nebula (into #{'re-cipes.networking.nebula} lean))

; Container/Virtualization

(def ^{:doc "KVM hypervisor ready"}
  kvm (into #{'re-cipes.infra.kvm 're-cipes.infra.zfs} base))

(def ^{:doc "Virtualization tools (KVM, LXC)"}
  virtual (into #{'re-cipes.infra.kvm 're-cipes.infra.lxd 're-cipes.packer} base))

(def ^{:doc "Container virtualization support"}
  conatiner (into #{'re-cipes.infra.lxd 're-cipes.packer} base))

(def ^{:doc "minikube"}
  minikube #{'re-cipes.infra.k8s 're-cipes.docker.server})

(def ^{:doc "Docker server"}
  docker (into #{'re-cipes.docker.server} base))

; Single purpose instance

(def ^{:doc "Bitwarden CLI"}
  bitwardedn (into #{'re-cipes.security.bitwarden} base))

(def ^{:doc "Backup tools"}
  backup (into #{'re-cipes.backup} base))

(def ^{:doc "Restore instance"}
  restore #{'re-cipes.restore 're-cipes.backup})

(def ^{:doc "Cloud tools (gcloud, doctl, awscli)"}
  cloud (into #{'re-cipes.cloud} base))

(def ^{:doc "An instance with just nvim"}
  editing #{'re-cipes.nvim})

(def ^{:doc "Security utilities"}
  security #{'re-cipes.security})

; Development profiles

(def ^{:doc "Base dev support"}
  base-dev (into #{'re-cipes.nvim} base))

(def ^{:doc "Dev with Syncthing enabled"}
  base-sync (into #{'re-cipes.syncthing} base-dev))

(def ^{:doc "Clojure development instance"}
  clj-dev (into #{'re-cipes.clojure} base-dev))

(def ^{:doc "Go development instance"}
  go-dev (into #{'re-cipes.development.go} base-dev))

(def ^{:doc "Hydroxide instance"}
  hydroxide (into #{'re-cipes.apps.hydroxide} go-dev))

(def ^{:doc "Signal CLI instance"}
  signal-cli (into #{'re-cipes.apps.signal-cli 're-cipes.development.jdk17} base))

(def ^{:doc "re-bot instance"}
  re-bot (into #{'re-cipes.apps.signal-cli 're-cipes.development.jdk17} clj-dev))

(def ^{:doc "Browser automation instance"}
  browser-drive (into #{'re-cipes.chromedriver} clj-dev))

(def ^{:doc "Development machine with Clojure and Graal"}
  native-clj (into #{'re-cipes.graal} clj-dev))

(def ^{:doc "Python development machine"}
  python-dev (into #{'re-cipes.nvim 're-cipes.python} base))

(def ^{:doc "Support for Java/Kotlin and Clojure development"}
  jvm-dev (into #{'re-cipes.intellij} clj-dev))

(def ^{:doc "Development machine with Clojure and deep learning utils"}
  learning (into #{'re-cipes.clojure 're-cipes.nvim 're-cipes.deep} base))

(def ^{:doc "Vuepress documentation"}
  vuepress (into #{'re-cipes.apps.vuepress 're-cipes.nvim} base))

(def ^{:doc "3d printing"}
  print3d (into #{'re-cipes.3dprint} base-dev))

(def ^{:doc "IoT development instance"}
  iot-dev (into #{'re-cipes.platformio 're-cipes.3dprint} base-dev))

; Desktop profiles

(def base-desktop (into #{'re-cipes.chrome} base))

(def lean-desktop (into #{'re-cipes.chrome} lean))

(def base-tilled (into base-desktop #{'re-cipes.xmonad}))

(def lean-tilled (into lean-desktop #{'re-cipes.xmonad}))

(def ^{:doc "Automated tilled dashboard"}
  dashboard (into lean-tilled #{'re-cipes.desktop.dashboard}))

(def ^{:doc "JVM dev desktop"}
  jvm-desktop (into base-desktop jvm-dev))

(def ^{:doc "IOT dev desktop"}
  iot-desktop (into base-desktop iot-dev))

(def ^{:doc "Clojure dev desktop"}
  clj-desktop (into base-desktop clj-dev))

(def ^{:doc "Zoom video"}
  zoom (into #{'re-cipes.zoom} lean-desktop))

(def ^{:doc "Android ADB"}
  adb (into #{'re-cipes.desktop.adb} lean-desktop))

(def ^{:doc "Signal messaging"}
  signal (into #{'re-cipes.desktop.signal} lean-desktop))

(def ^{:doc "Obsidian editor"}
  obsidian (into #{'re-cipes.desktop.obsidian} base-desktop))

(def ^{:doc "novnc server"}
  novnc (into #{'re-cipes.desktop.novnc} lean-desktop))


; Apps


(def ^{:doc "An app running within docker"} base-docker-app
  #{'re-cipes.hardening 're-cipes.docker.server 're-cipes.networking.nebula 're-cipes.monitoring})

(def ^{:doc "A webapp running on docker"} base-docker-webapp
  (into #{'re-cipes.docker.nginx} base-docker-app))

(def ^{:doc "An app which uses re-dock repo"} base-re_dock
  (into #{'re-cipes.docker.re-dock} base-docker-webapp))

(def ^{:doc "Full ELK stack"}
  elk (into #{'re-cipes.docker.elk-stack} base-re_dock))

(def ^{:doc "Elastisearch instance"}
  elasticsearch (into #{'re-cipes.apps.elasticsearch} base-re_dock))

(def ^{:doc "Influxdb server"}
  influxdb (into #{'re-cipes.apps.influxdb} base-docker-webapp))

(def ^{:doc "Influxdb for Home asisstant server"}
  influxdb-ha (into #{'re-cipes.apps.influxdb-ha} base-docker-webapp))

(def ^{:doc "Grafana instance"}
  grafana (into #{'re-cipes.apps.grafana} base-re_dock))

(def ^{:doc "Mosquitto instance"}
  mosquitto #{'re-cipes.apps.mosquitto 're-cipes.hardening 're-cipes.docker.server})

(def ^{:doc "Pfsense monitoring"}
  pfelk (into #{'re-cipes.apps.pfelk} base-docker-webapp))

(def ^{:doc "Tiddlywiki"}
  tiddlywiki (into #{'re-cipes.apps.tiddlywiki} base-docker-webapp))

(def ^{:doc "Photoprism service"}
  photoprism (into #{'re-cipes.apps.photoprism} base-docker-webapp))

(def ^{:doc "Artifactory instance"}
  artifactory (into #{'re-cipes.apps.artifactory} base-docker-webapp))

(def ^{:doc "Wazuh server"}
  wazuh (into #{'re-cipes.apps.wazuh} base-docker-app))

(def ^{:doc "Matrix and Element setup"}
  matrix (into #{'re-cipes.apps.matrix} base-re_dock))

; Non docker based apps

(def ^{:doc "A letsencrypt cert generation instance"} letsencrypt
  #{'re-cipes.hardening 're-cipes.apps.letsencrypt})

(def ^{:doc "apt-cache-ng"}
  apt-cache (into #{'re-cipes.apps.aptcache} base))

(def ^{:doc "squid-deb-proxy server"}
  squid-deb-proxy (into #{'re-cipes.apps.squiddebproxy} base))

; Notebook webbapp

(def ^{:doc "Python development machine"}
  jupyter (into clj-dev (into #{'re-cipes.nvim 're-cipes.apps.jupyter} base-docker-webapp)))

