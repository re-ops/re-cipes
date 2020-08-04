(ns re-cipes.lxd
  "Setting up lxd locally and adding it to the local lxc client"
  (:require
   re-cipes.hardening
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

(def-inline {:depends #'re-cipes.hardening/firewall} bridging
  "Enable bridge networking"
  []
  (add-interface "lxdbr0" :in :allow)
  (add-interface "lxdbr0" :out :allow)
  (enable-forwarding))

(def-inline {:depends #'re-cipes.lxd/server} remote
  "add local lxd instance locally"
  []
  (let [{:keys [lxd home]} (configuration)
        root (<< "~{home}/snap/lxd/current/.config/lxc")
        certfile (<< "~{root}/servercerts/127.0.0.1.crt")
        out (<< "~{root}/certificate.p12")
        key (<< "~{root}/client.key")
        crt (<< "~{root}/client.crt")
        pass (<< "pass:~(lxd :password)")]
    (letfn [(remove-remote [{:keys [bind]}]
              (fn []
                (script (pipe ("/usr/bin/lxc" "remote" "remove" ~bind) "true"))))
            (add-remote [{:keys [password bind]}]
                        (fn []
                          (script
                           ("/usr/bin/lxc" "remote" "add" ~bind "--password" ~password "--accept-certificate"))))
            (export []
                    (script
                     ("/usr/bin/openssl" "pkcs12" "-export" "-out" ~out "-inkey" ~key "-in" ~crt "-certfile" ~certfile "-passout" ~pass)))]
      (run (remove-remote lxd))
      (run (add-remote lxd))
      (run export))))
