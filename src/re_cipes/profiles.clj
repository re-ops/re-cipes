(ns re-cipes.profiles)

; Basic profiles
(def ^{:doc "Minimal set of recipes"}
  lean #{'re-cipes.access 're-cipes.shell 're-cipes.tmux 're-cipes.hardening 're-cipes.desktop})

(def ^{:doc "Base setup common to all plans (shell, hardening, osquery etc.)"}
  base (into #{'re-cipes.osquery 're-cipes.monitoring} lean))

; Infra profiles
(def ^{:doc "re-core ready instances"}
  core #{'re-cipes.clojure 're-cipes.build 're-cipes.nvim 're-cipes.shell 're-cipes.hardening})

(def nas (into #{'re-cipes.backup 're-cipes.zfs} base))

(def wireguard #{'re-cipes.hardening 're-cipes.wireguard})

; Container/Virtualization

(def ^{:doc "Virtualization tools (KVM, LXC)"}
  virtual (into #{'re-cipes.virtualization} base))

(def ^{:doc "minikube"}
  minikube #{'re-cipes.k8s 're-cipes.docker})

(def ^{:doc "Docker server"}
  docker (into #{'re-cipes.docker} base))

(def ^{:doc "Backup tools"}
  backup (into #{'re-cipes.backup} base))

(def ^{:doc "Restore instance"}
  restore (into #{'re-cipes.restore 're-cipes.backup} base))

(def ^{:doc "Cloud tools (gcloud, doctl, awscli)"}
  cloud (into #{'re-cipes.cloud} base))

(def ^{:doc "An instance with just nvim"}
  editing #{'re-cipes.nvim})

(def ^{:doc "Security utilities"}
  security #{'re-cipes.security})

; Development profiles

(def ^{:doc "Base dev support"}
  base-dev (into #{'re-cipes.build 're-cipes.nvim} lean))

(def ^{:doc "Clojure development instance"}
  clj-dev (into #{'re-cipes.clojure} base-dev))

(def ^{:doc "Development machine with Clojure and Graal"}
  native-clj (into #{'re-cipes.graal} clj-dev))

(def ^{:doc "Python development machine"}
  python-dev (into #{'re-cipes.nvim 're-cipes.python} lean))

(def ^{:doc "Support for Java/Kotlin and Clojure development"}
  jvm-dev (into #{'re-cipes.intellij} clj-dev))

(def ^{:doc "Development machine with Clojure and deep learning utils"}
  learning (into #{'re-cipes.clojure 're-cipes.build 're-cipes.nvim 're-cipes.deep} lean))

(def ^{:doc "A Vuepress documentation instance"}
  vuepress (into #{'re-cipes.node 're-cipes.nvim} lean))

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

