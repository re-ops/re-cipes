(ns re-cipes.desktop.signal
  "Cloud providers cli tools"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.common.recipe :refer (require-recipe)]
   [clojure.core.strint :refer (<<)]
   [re-cog.resources.package :refer (package key-file update-)]
   [re-cog.resources.file :refer (file line template copy directory)]
   [re-cog.resources.download :refer (download)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} signal
  "Installing gcloud client"
  []
  (let [{:keys [home]} (configuration)
        listing "/etc/apt/sources.list.d/signal-xenial.list"
        autostart (<< "~{home}/.config/autostart")
        key "signal.gpg"
        keyrings "/usr/share/keyrings/"
        repo (<< "deb [arch=amd64] https://updates.signal.org/desktop/apt xenial main")
        url "https://updates.signal.org/desktop/apt/keys.asc"]
    (download url (<< "~{keyrings}/~{key}") "2aca20b81a56ba0fbe24bdf58a45023e58a38392d885068afe596785ccd95491")
    (key-file (<< "~{keyrings}/~{key}"))
    (file listing :present)
    (line listing repo :present)
    (update-)
    (package "signal-desktop" :present)
    (directory autostart :present)
    (copy "/usr/share/applications/signal-desktop.desktop" (<< "~{autostart}/signal-desktop.desktop"))))
