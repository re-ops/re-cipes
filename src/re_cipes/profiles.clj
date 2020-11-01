(ns re-cipes.profiles)

; Basic profiles

(def ^{:doc "Minimal set of recipes"}
  lean #{'re-cipes.access 're-cipes.shell 're-cipes.tmux 're-cipes.hardening 're-cipes.desktop})

(def ^{:doc "Base setup common to all plans (shell, hardening, osquery etc.)"}
  base (into #{'re-cipes.monitoring} lean))

; Re-core

(def ^{:doc "Re-ops instance"}
  re-ops (into #{'re-cipes.re-ops.core 're-cipes.clojure 're-cipes.packer 're-cipes.nvim} lean))

(def ^{:doc "Re-ops standlone development instance with Docker and LXC enabled"}
  re-ops-standalone (into #{'re-cipes.re-ops.standalone 're-cipes.lxd 're-cipes.docker.server 're-cipes.apps.elasticsearch} re-ops))

(def ^{:doc "Re-ops packer image basic setup"}
  re-ops-image #{'re-cipes.hardening 're-cipes.re-ops.re-gent})

; Infra profiles

(def nas (into #{'re-cipes.backup 're-cipes.zfs} base))

(def wireguard #{'re-cipes.hardening 're-cipes.wireguard})

; Container/Virtualization

(def ^{:doc "KVM hypervisor ready"}
  kvm (into #{'re-cipes.kvm 're-cipes.zfs} base))

(def ^{:doc "Virtualization tools (KVM, LXC)"}
  virtual (into #{'re-cipes.kvm 're-cipes.lxd 're-cipes.packer} base))

(def ^{:doc "Container virtualization support"}
  conatiner (into #{'re-cipes.lxd 're-cipes.packer} base))

(def ^{:doc "minikube"}
  minikube #{'re-cipes.k8s 're-cipes.docker.server})

(def ^{:doc "Docker server"}
  docker (into #{'re-cipes.docker.server} base))

; Single purpose instance

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
  base-dev (into #{'re-cipes.nvim} lean))

(def ^{:doc "Clojure development instance"}
  clj-dev (into #{'re-cipes.clojure} base-dev))

(def ^{:doc "Browser automation instance"}
  browser-drive (into #{'re-cipes.chromedriver} clj-dev))

(def ^{:doc "Development machine with Clojure and Graal"}
  native-clj (into #{'re-cipes.graal} clj-dev))

(def ^{:doc "Python development machine"}
  python-dev (into #{'re-cipes.nvim 're-cipes.python} lean))

(def ^{:doc "Support for Java/Kotlin and Clojure development"}
  jvm-dev (into #{'re-cipes.intellij} clj-dev))

(def ^{:doc "Development machine with Clojure and deep learning utils"}
  learning (into #{'re-cipes.clojure 're-cipes.nvim 're-cipes.deep} lean))

(def ^{:doc "Vuepress documentation"}
  vuepress (into #{'re-cipes.apps.vuepress 're-cipes.nvim} lean))

(def ^{:doc "3d printing"}
  print3d (into #{'re-cipes.3dprint} base-dev))

(def ^{:doc "IoT development instance"}
  iot-dev (into #{'re-cipes.platformio 're-cipes.3dprint} base-dev))

; Desktop profiles

(def base-desktop #{'re-cipes.xmonad 're-cipes.chrome})

(def ^{:doc "JVM dev desktop"}
  jvm-desktop (into base-desktop jvm-dev))

(def ^{:doc "IOT dev desktop"}
  iot-desktop (into base-desktop iot-dev))

(def ^{:doc "Clojure dev desktop"}
  clj-desktop (into base-desktop clj-dev))

(def ^{:doc "Zoom instance"}
  zoom (into #{'re-cipes.zoom} base-desktop))

; Apps


(def ^{:doc "A letsencrypt certificate generation instance"} letsencrypt
  #{'re-cipes.hardening 're-cipes.apps.letsencrypt})

(def ^{:doc "base docker elk"} base-elk
  #{'re-cipes.hardening 're-cipes.docker.server 're-cipes.docker.nginx 're-cipes.docker.common})

(def ^{:doc "ELK stack"}
  elk (into #{'re-cipes.docker.elk-stack} base-elk))

(def ^{:doc "Elastisearch instance"}
  elasticsearch (into #{'re-cipes.apps.elasticsearch} base-elk))

(def ^{:doc "Grafana instance"}
  grafana (into #{'re-cipes.apps.grafana} base-elk))

(def ^{:doc "Grafana instance"}
  mosquitto #{'re-cipes.docker.mosquitto 're-cipes.hardening 're-cipes.docker.server})

(def ^{:doc "Pfsense monitoring"}
  pfelk #{'re-cipes.hardening 're-cipes.docker.server 're-cipes.docker.pfelk 're-cipes.docker.nginx})

(def ^{:doc "Tiddlywiki"}
  tiddlywiki (into #{'re-cipes.apps.tiddlywiki 're-cipes.docker.server 're-cipes.docker.nginx 're-cipes.nvim} lean))

(def ^{:doc "apt-cache-ng"}
  apt-cache (into #{'re-cipes.apps.aptcache} lean))

(def ^{:doc "squid-deb-proxy server"}
  squid-deb-proxy (into #{'re-cipes.apps.squiddebproxy} lean))
