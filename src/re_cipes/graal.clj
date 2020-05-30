(ns re-cipes.graal
  "Graal setup"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.file :refer (symlink directory)]
   [re-cog.resources.archive :refer (untar)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} install
  "Setting up Graal"
  []
  (letfn [(gu [bin pkg]
            (fn []
              (script (~bin "install" ~pkg))))]
    (let [{:keys [home]} (configuration)
          version "20.1.0"
          release (<< "graalvm-ce-java8-linux-amd64-~{version}")
          dest (<< "graalvm-ce-java8-~{version}")
          tmp (<< "/tmp/~{release}.tar.gz")
          expected "4fac212b37cd548831fd6587dd4d59dc068068815aa20323b47fde9529d6bb6e"
          url (<< "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-~{version}/~{release}.tar.gz")]
      (download url tmp expected)
      (untar tmp "/opt/")
      (directory (<< "~{home}/bin/") :present)
      (symlink (<< "~{home}/bin/gu") (<< "/opt/~{dest}/lib/installer/bin/gu"))
      (run (gu (<< "/opt/~{dest}/lib/installer/bin/gu") "native-image"))
      (symlink (<< "~{home}/bin/native-image") (<< "/opt/~{dest}/bin/native-image")))))

(def-inline native-code
  "Setting up native code compilation tools"
  []
  (package "build-essential" :present)
  (package "zlib1g-dev" :present))
