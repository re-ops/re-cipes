(ns re-cipes.apps.signal-cli
  "https://github.com/emersion/hydroxide setup"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (symlink directory)]
   [re-cog.resources.systemd :refer (set-service)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} signal-cli
  "Setting up signal-cli"
  []
  (let [version "0.10.5"
        sum "26254ef650c1d6913d9af411a152926bbc7be6f93625cf0e1456d16b45e77b22"
        artifact (<< "signal-cli-~{version}-Linux.tar.gz")
        dest (<< "/usr/src/~{artifact}")
        url (<< "https://github.com/AsamK/signal-cli/releases/download/v~{version}/~{artifact}")
        bin "/usr/local/bin/signal-cli"
        {:keys [home]} (configuration)]
    (download url dest sum)
    (untar dest "/opt/")
    (symlink bin (<< "/opt/signal-cli-~{version}/bin/signal-cli"))
    (chmod bin "+x" {})))

(def-inline signal-cli-service
  "Setting up signal-cli service (make sure to link the device before starting it) "
  []
  (let [{:keys [user]} (configuration)
        {:keys [number log]} (configuration :signal-cli)
        cmd (<< "/usr/local/bin/signal-cli -a ~{number} -o json daemon --socket --log-file ~{log}")
        opts {:wants "basic.target"
              :after "basic.target network.target"
              :restart "always"
              :stop "/bin/kill -HUP $MAINPID"
              :wanted-by "multi-user.target"
              :environment {:JAVA_HOME "/usr/lib/jvm/java-17-amazon-corretto"}
              :user user}]
    (set-service "signal-cli" "Signal cli daemo process" cmd opts)))
