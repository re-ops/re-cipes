(ns re-cipes.intellij
  "Setting up Intellij"
  (:require
   [re-cipes.shell :refer [dot-files]]
   [re-cipes.access :refer [permissions]]
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.archive :refer (untar)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.file :refer (file line)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.access/permissions #'re-cipes.shell/dot-files]} install
  "Setting up Intellij Idea community edition"
  []
  (let [{:keys [home]} (configuration)
        version "ideaIC-2019.3.1"
        tmp (<< "/tmp/~{version}.tar.gz")
        expected "b67cc055d7ab18b2a864d05956407ae1f910eb295e2a73e6a6aa813260930509"
        url (<< "https://download-cf.jetbrains.com/idea/~{version}.tar.gz")]
    (download url tmp expected)
    (untar tmp "/opt/")
    (directory (<< "~{home}/bin/") :present)))

(def-inline inotify-max
  "Change the number of inotify watches"
  []
  (letfn [(sysctl-reload [target]
            (fn []
              (script ("/sbin/sysctl" "-e" "-p" ~target))))]
    (let [target "/etc/sysctl.d/10-notify-watch.conf"]
      (set-file-acl "re-ops" "rwX" "/etc/sysctl.d")
      (file target :present)
      (line target "fs.inotify.max_user_watches = 524288" :present)
      (run (sysctl-reload target)))))
