(ns re-cipes.desktop.novnc
  "Setting up novnc"
  (:require
   [re-cog.resources.file :refer (copy directory)]
   [re-cog.resources.systemd :refer (set-service)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline websockityf
  "Installing websockityf"
  []
  (let [{:keys [home user]} (configuration)
        config {:user user :restart true}]
    (package "websockify" :present)
    (package "python-numpy" :present)
    (clone "https://github.com/novnc/noVNC.git" (<< "~{home}/novnc") {})
    #_(set-service "websockityf" "Launching novnc service" (<< "/usr/bin/websockify --web=~{home}/novnc --cert=/etc/ssl/novnc.pem 6080 localhost:5901") config)))

(def-inline tightvnc
  "Setting up vnc server"
  []
  (let [{:keys [home user]} (configuration)
        config {:stop "/usr/bin/vncserver -kill :1" :user user}]
    (package "tightvncserver" :present)
    (directory (<< "~{home}/.vnc") :present)
    (copy "/tmp/resources/templates/novnc/xstartup" (<< "~{home}/.vnc/xstartup"))
    (set-service "tightvnc" "Launching novnc service" (<< "/usr/bin/vncserver -geometry 1800x1000 -depth 16 :1") config)))
