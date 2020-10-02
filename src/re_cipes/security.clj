(ns re-cipes.security
  "Common tools"
  (:require
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]))

(require-recipe)

(def-inline password
  "Password generation"
  []
  (package "pwgen" :present))

(def-inline veracrypt
  "Instaling veracrypt"
  []
  (let [version "1.24-Update7"
        url-version (clojure.string/lower-case version)
        deb (<< "veracrypt-console-~{version}-Ubuntu-20.04-amd64.deb")
        url (<< "https://launchpad.net/veracrypt/trunk/~{url-version}/+download/~{deb}")
        sum "3c8128451480ff31ce10c92e169a5f94"]
    (download url (<< "/tmp/~{deb}") sum :hash-type :md5)
    (package (<< "/tmp/~{deb}") :present)))
