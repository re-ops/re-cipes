(ns re-cipes.desktop.novnc
  "Setting up novnc"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.exec :refer [run]]
   [re-cog.resources.file :refer (copy directory)]
   [re-cog.resources.systemd :refer (set-service)]
   [re-cog.facts.datalog :refer (hostname)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} websockify
  "Installing websockify"
  []
  (let [{:keys [home user]} (configuration)
        config {:user user :restart true}
        cli (<< "/usr/bin/websockify --web=~{home}/novnc --cert=/etc/ssl/cert.pem  --key=/etc/ssl/privkey.pem 443 localhost:5901")]
    (package "websockify" :present)
    (package "python-numpy" :present)
    (clone "https://github.com/novnc/noVNC.git" (<< "~{home}/novnc") {})
    (set-service "websockitfy" "Launching novnc service" cli config)))

(def-inline {:depends #'re-cipes.access/permissions} tightvnc
  "Setting up vnc server"
  []
  (let [{:keys [home user]} (configuration)
        config {:stop "/usr/bin/vncserver -kill :1" :user user}
        cli (<< "/usr/bin/vncserver -geometry 1800x1000 -depth 16 :1")]
    (package "tightvncserver" :present)
    (directory (<< "~{home}/.vnc") :present)
    (copy "/tmp/resources/templates/novnc/xstartup" (<< "~{home}/.vnc/xstartup"))
    (chmod  (<< "~{home}/.vnc/xstartup") "+x" {})
    (set-service "tightvnc" "Launching novnc service" cli config)))

(def-inline {:depends #'re-cipes.desktop.novnc/tightvnc} vncpassword
  "Setting vnc password"
  []
  (letfn [(passwd [pass target]
            (fn []
              (script
               (pipe
                (pipe ("/usr/bin/echo" ~pass) ("/usr/bin/vncpasswd" "-f")) ("/usr/bin/tee" ~target)))))]
    (let [{:keys [home user]} (configuration)
          {:keys [passwords]} (configuration :novnc)]
      (run (passwd (passwords (hostname)) (<< "~{home}/.vnc/passwd"))))))
