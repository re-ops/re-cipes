(ns re-cipes.chrome
  "Google chrome"
  (:require
   [re-cipes.access :refer (permissions)]
   [clojure.core.strint :refer (<<)]
   [re-cog.resources.file :refer (file line)]
   [re-cog.resources.package :refer (package key-file update-)]
   [re-cog.resources.user :refer (group-add)]
   [re-cog.resources.download :refer (download)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} install
  "Install google chrome"
  []
  (let [sources "/etc/apt/sources.list.d"
        listing (<< "~{sources}/google-chrome.list")
        url "https://dl-ssl.google.com/linux/linux_signing_key.pub"
        keyrings "/usr/share/keyrings"
        key "google-chrome-key"
        repo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main"]
    (download url (<< "~{keyrings}/~{key}") "c9e8f825be3168600804973fdd41e47dd26a9b29d6b1a8b6fcab31758daced8c")
    (key-file (<< "~{keyrings}/~{key}"))
    (file listing :present)
    (line listing repo :present)
    (update-)
    (package "google-chrome-stable" :present)))
