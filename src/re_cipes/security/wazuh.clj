(ns re-cipes.security.wazuh
  "Setting up Wazuh agent"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.common.recipe :refer (require-recipe)]
   [clojure.core.strint :refer (<<)]
   [re-cog.resources.archive :refer (untar)]
   [re-cog.resources.package :refer (package key-file update-)]
   [re-cog.resources.file :refer (file line)]
   [re-cog.resources.download :refer (download)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} agent-
  "Installing wazuh agent"
  []
  (let [listing "/etc/apt/sources.list.d/wazuh.list"
        key "GPG-KEY-WAZUH"
        keyrings "/usr/share/keyrings/"
        repo (<< "deb https://packages.wazuh.com/4.x/apt/ stable main")
        url "https://packages.wazuh.com/key/GPG-KEY-WAZUH"]
    (download url (<< "~{keyrings}/~{key}") "a378ca8dfa6b72122df288f64a0cde54f1cbfa3db9b43e6865cb73def35b5b17")
    (key-file (<< "~{keyrings}/~{key}"))
    (file listing :present)
    (line listing repo :present)
    (update-)
    (package "wazuh-agent" :present)))
