(ns re-cipes.backup
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.file :refer (rename symlink chmod)]
   [re-cog.resources.archive :refer (untar bzip2)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} restic
  "Setting up restic"
  []
  (let [version "0.9.6"
        release (<< "restic_~{version}_linux_amd64")
        tmp (<< "/tmp/~{release}.bz2")
        expected "a88ca09d1dd051d470965667a224a2b81930c6628a0566b7b17868be40207dc8"
        url (<< "https://github.com/restic/restic/releases/download/v~{version}/~{release}.bz2")]
    (download url tmp expected)
    (bzip2 tmp)
    (rename (<< "/tmp/~{release}") "/usr/bin/restic")
    (chmod "/usr/bin/restic" "0755")))

(def-inline {:depends #'re-cipes.access/permissions} octo
  "Setting up octo"
  []
  (let [version "0.8.1"
        tmp "/tmp/octo"
        expected "c53abdfd81fc5eb48ff138faf3cdcd11acd7a089a44d0d82c05a63a56ef691ee"
        url (<< "https://github.com/narkisr/octo/releases/download/~{version}/octo")]
    (download url tmp expected)
    (rename tmp "/usr/bin/octo")
    (chmod "/usr/bin/octo" "0755")))

(def-inline zbackup
  "Setting up zbackup"
  []
  (package "zbackup" :present))

(def-inline rclone
  "Setting up rclone"
  []
  (package "rclone" :present))
