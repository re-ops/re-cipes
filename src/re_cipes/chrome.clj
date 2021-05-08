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
    (download url (<< "~{keyrings}/~{key}") "5dfdccb6f67eea65aeb717f1ef90a81fa80fe77c60f4d3dfdf45c969748069c5")
    (key-file (<< "~{keyrings}/~{key}"))
    (file listing :present)
    (line listing repo :present)
    (update-)
    (package "google-chrome-stable" :present)))
