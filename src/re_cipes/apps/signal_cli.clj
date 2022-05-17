(ns re-cipes.apps.signal-cli
  "https://github.com/emersion/hydroxide setup"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (symlink)]
   [re-cog.resources.systemd :refer (set-service)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} signal-cli
  "Setting up signal-cli"
  []
  (let [version "0.10.15"
        sum "c6586cd1c5bfc6b57c3bf5fc33a1e10bff7f07cc2f07d00ab507ad549e3a1bdf"
        artifact (<< "signal-cli_~{version}_Linux_.tar.gz")
        dest (<< "/usr/src/~{artifact}")
        url (<< "https://github.com/AsamK/signal-cli/releases/download/v~{version}/~{artifact}")]
    (download url dest sum)
    (untar dest "/opt/")
    (symlink "/usr/local/bin/signal-cli" (<< "/opt/signal-cli-~{version}"))))

(def-inline signal-cli-service
  "Setting up signal-cli service (make sure to link the device before starting it) "
  []
  (let [{:keys [user]} (configuration)
        {:keys [number]} (configuration :signal-cli)
        cmd (<< "/usr/local/bin/signal-cli -a ~{number} -o json daemon --socket")
        opts {:wants "basic.target"
              :after "basic.target network.target"
              :restart "always"
              :stop "/bin/kill -HUP $MAINPID"
              :wanted-by "multi-user.target"
              :user user}]
    (set-service "signal-cli" "Signal cli daemo process" cmd opts)))
