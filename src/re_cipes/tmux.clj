(ns re-cipes.tmux
  "Setting up tmux and related utilities"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.git :refer (clone)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.file :refer (symlink directory chmod)]))

(require-recipe)

(def-inline tmux
  "Setup tmux for user"
  []
  (let [{:keys [home user]} (configuration)
        dest (<< "~{home}/.tmux")]
    (package "tmux" :present)
    (clone "git://github.com/narkisr/.tmux.git" dest)
    (directory (<< "~{dest}/plugins/") :present)
    (clone "git://github.com/tmux-plugins/tpm" (<< "~{dest}/plugins/tpm"))
    (symlink (<< "~{home}/.tmux.conf") (<< "~{dest}/.tmux.conf"))
    (chown dest user user {})))

(def-inline {:depends #'re-cipes.access/permissions} tmx
  "Setting up tmx https://github.com/narkisr/tmx"
  []
  (let [version "0.2.2"
        sum "5d624f40caef8b8c5f8b420474778499f55ffeab7a2fc19c892d00aa20f05c70"
        url (<< "https://github.com/narkisr/tmx/releases/download/~{version}/tmx")
        dest "/usr/local/bin/tmx"]
    (download url dest sum)
    (chmod dest "+x")))
