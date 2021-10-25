(ns re-cipes.apps.jupyter
  "Setting up Jupyter notebook"
  (:require
   [re-cipes.hardening]
   [re-cog.resources.download :refer (download)]
   [re-cipes.docker.nginx]
   [re-cog.resources.exec :refer (run)]
   [re-cog.resources.systemd :refer (set-service)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.file :refer (symlink directory)]))

(require-recipe)

(def-inline anaconda
  "Setting up Anaconda"
  []
  (let [{:keys [home user]} (configuration)
        password (configuration :jupyter :password)
        binary "Anaconda3-2021.05-Linux-x86_64.sh"
        url (<< "https://repo.anaconda.com/archive/~{binary}")
        target (<< "/usr/src/~{binary}")
        anaconda-home (<< "~{home}/anaconda")
        jupyter-bin (<< "~{anaconda-home}/bin/jupyter")
        jupyter-config (<< "~{home}/.jupyter/jupyter_notebook_config.py")
        python3 (<< "~{anaconda-home}/bin/python3")
        passwd (<< "\"from notebook.auth import passwd; print(passwd('~{password}'))\"")]
    (letfn [(anaconda []
              (script
               (if (file-exists? ~anaconda-home)
                 ("exit" 0))
               ("/usr/bin/bash" ~target "-b" "-p" ~anaconda-home)))
            (set-password []
                          (script
                           (set! PASS @(~python3 "-c" ~passwd))
                           (~jupyter-bin "notebook" "--generate-config" "-y")
                           ("echo" (quoted "c.NotebookApp.password='${PASS}'") >> ~jupyter-config)))]
      (download url target "2751ab3d678ff0277ae80f9e8a74f218cfc70fe9a9cdc7bb1c137d7e47e33d53")
      (run anaconda)
      (run set-password))))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]} nginx
  "Nginx site enable"
  []
  (let [external-port 443
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "jupyter" external-port 8888 {:basic-auth false})
    (add-rule external-port :allow {})))
