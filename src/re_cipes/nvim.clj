(ns re-cipes.nvim
  "Setting up NeoVim"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.file :refer (symlink directory chmod chown)]
   [re-cog.resources.git :refer (clone)]
   [re-cog.resources.archive :refer (untar)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} nvim
  "Installing Neovim"
  []
  (let [version "0.3.8"
        archive "nvim-linux64.tar.gz"
        url (<< "https://github.com/neovim/neovim/releases/download/v~{version}/~{archive}")
        sum "30440b0eb4eb0adbf09c458891e81880c9efe5245b52316e9af1c08136088e6a"
        dest (<< "/usr/src/~{archive}")]
    (download url dest sum)
    (untar dest "/opt/")
    (symlink "/usr/local/bin/nvim" "/opt/nvim-linux64/bin/nvim")))

(def-inline config
  "Configure nvim"
  []
  (let [{:keys [home user]} (configuration)
        config (<< "~{home}/.config/nvim")]
    (clone "https://github.com/narkisr/nvim.git" config {})
    (chown config user user {:recursive true})))

(def-inline powerline
  "Install powerline"
  []
  (let [{:keys [home user]} (configuration)
        fonts (<< "~{home}/.fonts")
        repo "https://github.com/scotu/ubuntu-mono-powerline.git"]
    (directory fonts :present)
    (clone repo (<< "~{fonts}/ubuntu-mono-powerline") {})
    (chown fonts user user {:recursive true})))
