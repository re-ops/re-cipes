(ns re-cipes.security.wazuh
  "Setting up Wazuh agent"
  (:require
   [re-cog.resources.exec :refer [run]]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cipes.access :refer (permissions)]
   [re-cog.common.recipe :refer (require-recipe)]
   [clojure.core.strint :refer (<<)]
   [re-cog.resources.package :refer (package key-file update-)]
   [re-cog.resources.file :refer (file line)]
   [re-cog.resources.download :refer (download)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} install
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

(def-inline {:depends [#'re-cipes.security.wazuh/install #'re-cipes.access/permissions]} agent-
  "Configure agent"
  []
  (let [{:keys [wazuh]} (configuration)
        ip (-> wazuh :manager :ip)
        parent "/var/ossec/"
        config (<< "~{parent}/etc/")
        file (<< "~{config}/ossec.conf")
        initial  "      <address>MANAGER_IP</address>"
        configured (<< "      <address>~{ip}</address>")]
    (set-file-acl "re-ops" "rx" parent)
    (set-file-acl "re-ops" "rw" file)
    (line file initial :replace :with configured)))

(def-inline {:depends [#'re-cipes.security.wazuh/agent-]} registeration
  "Configure agent"
  []
  (let [{:keys [wazuh]} (configuration)
        ip (configuration :wazuh :manager :ip)
        pass (configuration :wazuh :passwords :agent)]
    (letfn [(register []
              (script
               ("sudo" "/var/ossec/bin/agent-auth" "-m" ~ip "-P" ~pass)))]
      (run register))))
