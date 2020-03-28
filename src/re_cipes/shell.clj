(ns re-cipes.shell
  "Setting up shell"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.git :refer (clone)]
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.file :refer (symlink directory chmod)]))

(require-recipe)

(def-inline zsh
  "zsh setup"
  []
  (letfn [(chsh [user]
            (fn []
              (script ("sudo" "/usr/bin/chsh" "-s" "/usr/bin/zsh" ~user))))]
    (let [{:keys [home user]} (configuration)
          dest (<< "~{home}/.tmux")]
      (package "zsh" :present)
      (when-not  (clojure.string/includes? (<< "~{user}:/bin/zsh") (slurp "/etc/passwd"))
        (run (chsh user))))))

(def-inline minimal-zsh
  "Minmal zsh setup"
  []
  (let [{:keys [home user]} (configuration)
        dest (<< "~{home}/.minimal-zsh")]
    (clone "git://github.com/narkisr/minimal-zsh.git" dest)
    (chown dest user user {})
    (symlink (<< "~{home}/.zshrc") (<< "~{dest}/.zshrc"))))

(def-inline {:depends #'re-cipes.access/permissions} z
  "rupa z"
  []
  (clone "git://github.com/rupa/z.git" "/opt/z"))

(def-inline dot-files
  "Setting up dot files from git://github.com/narkisr/dots.git"
  []
  (let [{:keys [home user]} (configuration)
        dest (<< "~{home}/.dots")]
    (clone "git://github.com/narkisr/dots.git" dest)
    (chown dest user user {})))

(def-inline {:depends #'re-cipes.shell/dot-files} ack
  "ack grep setup"
  []
  (let [{:keys [home]} (configuration)
        dots (<< "~{home}/.dots")]
    (package "ack" :present)
    (symlink (<< "~{home}/.ackrc") (<< "~{dots}/.ackrc"))))

(def-inline {:depends #'re-cipes.shell/dot-files} rlwrap
  "rlwrap setup"
  []
  (let [{:keys [home]} (configuration)
        dots (<< "~{home}/.dots")]
    (package "rlwrap" :present)
    (symlink  (<< "~{home}/.inputrc") (<< "~{dots}/.inputrc"))))

(def-inline fd
  "fd a friendly alternative to find"
  []
  (let [version "7.4.0"
        artifact (<< "fd_~{version}_amd64.deb")
        url (<< "https://github.com/sharkdp/fd/releases/download/v~{version}/~{artifact}")
        sum "e141dbd0066ca75ac2a2d220226587f7ac1731710376300ad7d329c79110f811"]
    (download url (<< "/tmp/~{artifact}") sum)
    (package (<< "/tmp/~{artifact}") :present)))

(def-inline bat
  "bat a modern cat"
  []
  (let [version "0.12.1"
        artifact (<< "bat_~{version}_amd64.deb")
        url (<< "https://github.com/sharkdp/bat/releases/download/v~{version}/~{artifact}")
        sum "c02ca23add052009cde64746ff86e6da5765a89fd7494d380800250310180b23"]
    (download url (<< "/tmp/~{artifact}") sum)
    (package (<< "/tmp/~{artifact}") :present)))
