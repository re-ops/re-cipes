(ns re-cipes.platformio
  "Setting up https://platformio.org"
  (:require
   [re-cipes.python :refer [python-3]]
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.package :refer (package key-file update-)]
   [re-cog.resources.file :refer (file line)]))

(require-recipe)

(def-inline {:depends #'re-cipes.python/python-3} core
  "Setting up platformio core package"
  []
  (letfn [(install []
            (script ("/usr/bin/pip3" "install" "-U" "platformio")))]
    (run install)))

(def-inline prequisits
  "docker install prequisits"
  []
  (doseq [p ["apt-transport-https" "ca-certificates" "gnupg-agent" "software-properties-common"]]
    (package p :present)))

(def-inline {:depends #'re-cipes.platformio/prequisits} vcode
  "Install vscode"
  []
  (let [sources "/etc/apt/sources.list.d"
        listing (<< "~{sources}/vscode.list")
        url "https://packages.microsoft.com/keys/microsoft.asc"
        keyrings "/usr/share/keyrings"
        key "packages.microsoft.gpg"
        repo (<< "deb [arch=amd64] https://packages.microsoft.com/repos/vscode stable main")]
    (download url (<< "~{keyrings}/~{key}") "2cfd20a306b2fa5e25522d78f2ef50a1f429d35fd30bd983e2ebffc2b80944fa")
    (key-file (<< "~{keyrings}/~{key}"))
    (file listing :present)
    (line listing repo :present)
    (update-)
    (package "code" :present)))

(def-inline {:depends #'re-cipes.platformio/vcode} platform-ide
  "Installing vscode platform-ide extensions"
  []
  (letfn [(install []
            (script
             ("/usr/bin/code" "--install-extension" "platformio.platformio-ide")
             ("/usr/bin/code" "--install-extension" "vscodevim.vim")))]
    (run install)))
