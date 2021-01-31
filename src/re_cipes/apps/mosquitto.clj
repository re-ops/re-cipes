(ns re-cipes.apps.mosquitto
  (:require
   [re-cipes.docker.server]
   [re-cipes.hardening]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.file :refer (copy directory chmod line)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.docker.server/services]} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        compose "/etc/docker/compose/mosquitto"]
    (clone repo compose {})
    (directory (<< "~{compose}/mosquitto-conf") :present)
    (copy (<< "~{compose}/mosquitto.yml") (<< "~{compose}/docker-compose.yml"))
    (on-boot "docker-compose@mosquitto" :enable)))

(def-inline {:depends [#'re-cipes.apps.mosquitto/get-source #'re-cipes.hardening/firewall]} configure
  "Setting up configuration options"
  []
  (let [compose "/etc/docker/compose/mosquitto"
        conf (<< "~{compose}/mosquitto-conf/mosquitto.conf")
        port (configuration :mosquitto :port)
        options {:allow_anonymous false
                 :password_file "/mosquitto/config/mosquitto-passwd"
                 :port port}]
    (add-rule port :allow {})
    (file conf :present)
    (doseq [[option value] options]
      (line conf (<< "~(name option) ~{value}") :present))))

(def-inline {:depends [#'re-cipes.apps.mosquitto/get-source]} passwd
  "Mosquitto password file"
  []
  (let [compose "/etc/docker/compose/mosquitto"
        passwd (<< "~{compose}/mosquitto-conf/mosquitto-passwd")
        passwords (configuration :mosquitto :passwords)]
    (file passwd :present)
    (doseq [{:keys [user hash]} passwords]
      (line passwd (<< "~{user}:~{hash}") :present))))
