(ns re-cipes.syncthing
  "Setting up syncthing"
  (:require
   [re-cog.resources.service :refer (on-boot service)]
   [re-cipes.access :refer (permissions)]
   [clojure.core.strint :refer (<<)]
   [re-cog.resources.file :refer (file line)]
   [re-cog.resources.package :refer (package key-file update-)]
   [re-cog.resources.download :refer (download)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} install
  "Setup syncthing repo and package"
  []
  (let [sources "/etc/apt/sources.list.d"
        listing (<< "~{sources}/syncthing.list")
        url "https://syncthing.net/release-key.gpg"
        keyrings "/usr/share/keyrings"
        key "syncthing-archive-keyring.gpg"
        repo "deb [signed-by=/usr/share/keyrings/syncthing-archive-keyring.gpg] https://apt.syncthing.net/ syncthing stable"]
    (download url (<< "~{keyrings}/~{key}") "a3806c3511f2cce3d2f12962f64b58b9192a15c9d862886cc46f9de8a25d7dbf")
    (key-file (<< "~{keyrings}/~{key}"))
    (file listing :present)
    (line listing repo :present)
    (update-)
    (package "syncthing" :present)))

(def-inline {:depends #'re-cipes.syncthing/install} enable-syncthing
  "Enable syncthing for user"
  []
  (let [{:keys [user]} (configuration)]
    (on-boot (<< "syncthing@~{user}") :enable)
    (service (<< "syncthing@~{user}") :restart)))
