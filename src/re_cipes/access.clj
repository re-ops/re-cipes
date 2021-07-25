(ns re-cipes.access
  (:require
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.permissions :refer (set-file-acl)]))

(require-recipe)

(def-inline permissions
  "Enabling re-ops folder access"
  []
  (let [{:keys [user]} (configuration)]
    (doseq [path ["/opt/" "/usr/local/bin/" "/usr/local/etc/" "/usr/src/"
                  "/usr/share/keyrings/" "/etc/apt/sources.list.d/"
                  "/etc/security/limits.d/" "/etc/modprobe.d/"]]
      (when (= user "re-ops")
        (set-file-acl "re-ops" "rwx" path)))))
