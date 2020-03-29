(ns re-cipes.cloud
  "Cloud providers cli tools"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [clojure.core.strint :refer (<<)]
   [re-cog.resources.archive :refer (untar)]
   [re-cog.resources.package :refer (package key-file update-)]
   [re-cog.resources.file :refer (file line)]
   [re-cog.resources.download :refer (download)]))

(require-recipe)

(def-inline gcloud
  "Installing gcloud client"
  []
  (let [listing "/etc/apt/sources.list.d/google-cloud-sdk.list"
        key "cloud.google.gpg"
        keyrings "/usr/share/keyrings/"
        repo (<< "deb [signed-by=~{keyrings}/~{key}] http://packages.cloud.google.com/apt cloud-sdk main")
        url "https://packages.cloud.google.com/apt/doc/apt-key.gpg"]
    (download url (<< "~{keyrings}/~{key}") "1fe629470162c72777c1ed5e5b0f392acf403cf6a374cb229cf76109b5c90ed5")
    (key-file (<< "~{keyrings}/~{key}"))
    (file listing :present)
    (line listing repo :present)
    (update-)
    (package "google-cloud-sdk" :present)))

(def-inline {:depends #'re-cipes.cloud/gcloud} emulators
  "Installing gcloud emulators (pubsub)"
  []
  (package "google-cloud-sdk-pubsub-emulator" :present))

(def-inline {:depends #'re-cipes.access/permissions} doctl
  "Digitalocean client"
  []
  (let [version "1.32.2"
        artifact (<< "doctl-~{version}-linux-amd64.tar.gz")
        url (<< "https://github.com/digitalocean/doctl/releases/download/v~{version}/~{artifact}")
        sum "6b961d9350965655097ce429fab3ded6719b283398dcffcb624da73740cf3bf9"
        dest (<< "/usr/src/~{artifact}")]
    (download url dest sum)
    (untar dest "/usr/local/bin/")))
