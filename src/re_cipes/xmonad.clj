(ns re-cipes.xmonad
  "Xmonad setup"
  (:require
   [re-cog.resources.git :refer (clone)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.file :refer (file line)]))

(require-recipe)

(def-inline install
  "Installing Xmonad"
  []
  (package "xmonad" :present)
  (package "gnome-terminal" :present))

(def-inline {:depends #'re-cipes.xmonad/install} configure
  "configure Xmonad"
  []
  (letfn [(recompile []
            (script ("xmonad" "--recompile")))]
    (let [{:keys [home user]} (configuration)
          repo "git://github.com/narkisr/xmonad-config.git"
          config (<< "~{home}/.xmonad")]
      (clone repo config)
      (chown config user user {:recursive true})
      (run recompile))))
