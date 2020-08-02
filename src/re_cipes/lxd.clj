(ns re-cipes.lxd
  "Setting up lxd locally and adding it to the local lxc client"
  (:require
   [re-cog.resources.ufw :refer (add-interface enable-forwarding)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.file :refer (template)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.exec :refer [run]]))

(require-recipe)

(def-inline server
  "Installing lxd"
  []
  (let [{:keys [lxd]} (configuration)]
    (letfn [(init []
              (script
               (pipe
                ("/usr/bin/cat" "/tmp/preseed.yaml")
                ("/usr/bin/lxd" "init" "--preseed"))))]
      (package "lxd" :present)
      (package "zfsutils-linux" :present)
      (template "/tmp/resources/templates/lxd/preseed.mustache" "/tmp/preseed.yaml" lxd)
      (run init))))

(def-inline bridging
  "Enable bridge networking"
  []
  (add-interface "lxdbr0" :in :allow)
  (add-interface "lxdbr0" :out :allow)
  (enable-forwarding))

(def-inline {:depends #'re-cipes.lxd/server} remote
  "add local lxd instance locally"
  []
  (let [{:keys [lxd home]} (configuration)
        {:keys [password]} lxd
        root (<< "~{home}/snap/lxd/current/.config/lxc")
        certfile (<< "~{root}/servercerts/127.0.0.1.crt")
        out (<< "~{root}/certificate.p12")
        key (<< "~{root}/client.key")
        crt (<< "~{root}/client.crt")
        pass (<< "pass:~{password}")]
    (letfn [(remove-remote [bind]
              (fn []
                (script (pipe ("/usr/bin/lxc" "remove" ~bind) "true"))))
            (add-remote [password]
                        (fn []
                          (script
                           ("/usr/bin/lxc" "remote" "add" "127.0.0.1" "--password" ~password "--accept-certificate"))))
            (export []
                    (script
                     ("/usr/bin/openssl" "pkcs12" "-export" "-out" ~out "-inkey" ~key "-in" ~crt "-certfile" ~certfile "-passout" ~pass)))]
      (run (remove-remote (lxd :bind)))
      (run (add-remote password))
      (run export))))
