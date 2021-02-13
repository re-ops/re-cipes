(ns re-cipes.shell
  "Setting up shell"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.git :refer (clone)]
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.file :refer (symlink directory)]))

(require-recipe)

(def-inline zsh
  "zsh setup"
  []
  (letfn [(chsh [user]
            (fn []
              (script ("sudo" "/usr/bin/chsh" "-s" "/usr/bin/zsh" ~user))))]
    (let [{:keys [home user]} (configuration)]
      (package "zsh" :present)
      (when-not  (clojure.string/includes? (<< "~{user}:/bin/zsh") (slurp "/etc/passwd"))
        (run (chsh user))))))

(def-inline minimal-zsh
  "Minmal zsh setup"
  []
  (let [{:keys [home user]} (configuration)
        dest (<< "~{home}/.minimal-zsh")]
    (clone "https://github.com/narkisr/minimal-zsh.git" dest {})
    (chown dest user user {})
    (symlink (<< "~{home}/.zshrc") (<< "~{dest}/.zshrc"))
    (chown (<< "~{home}/.zshrc") user user {})))

(def-inline {:depends #'re-cipes.access/permissions} z
  "rupa z"
  []
  (clone "https://github.com/rupa/z.git" "/opt/z" {}))

(def-inline dot-files
  "Setting up dot files from git://github.com/narkisr/dots.git"
  []
  (let [{:keys [home user]} (configuration)
        dest (<< "~{home}/.dots")]
    (clone "https://github.com/narkisr/dots.git" dest {})
    (chown dest user user {:recursive true})))

(def-inline {:depends #'re-cipes.shell/dot-files} ack
  "ack grep setup"
  []
  (let [{:keys [home user]} (configuration)
        dots (<< "~{home}/.dots")]
    (package "ack" :present)
    (symlink (<< "~{home}/.ackrc") (<< "~{dots}/.ackrc"))
    (chown (<< "~{home}/.ackrc") user user {})))

(def-inline {:depends #'re-cipes.shell/dot-files} rlwrap
  "rlwrap setup"
  []
  (let [{:keys [home user]} (configuration)
        dots (<< "~{home}/.dots")]
    (package "rlwrap" :present)
    (symlink  (<< "~{home}/.inputrc") (<< "~{dots}/.inputrc"))
    (chown (<< "~{home}/.inputrc") user user {})))

(def-inline fd
  "fd a friendly alternative to find"
  []
  (let [version "7.4.0"
        artifact (<< "fd_~{version}_amd64.deb")
        url (<< "https://github.com/sharkdp/fd/releases/download/v~{version}/~{artifact}")
        sum "e141dbd0066ca75ac2a2d220226587f7ac1731710376300ad7d329c79110f811"]
    (download url (<< "/tmp/~{artifact}") sum)
    (package (<< "/tmp/~{artifact}") :present)))
