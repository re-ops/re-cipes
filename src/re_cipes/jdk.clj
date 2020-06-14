(ns re-cipes.jdk
  "Setting up the jdk"
  (:require
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]))

(require-recipe)

(def-inline corretto
  "Setting up Amazon corretto 8 jdk"
  []
  (let [version "8.252.09-1"
        url-version (.replace version "-" ".")
        deb (<< "java-1.8.0-amazon-corretto-jdk_~{version}_amd64.deb")
        url (<< "https://corretto.aws/downloads/resources/~{url-version}/java-1.8.0-amazon-corretto-jdk_~{version}_amd64.deb")
        sum "bc266354a0a02aaf93ecc5062ee24ff8"]
    (download url (<< "/tmp/~{deb}") sum :hash-type :md5)
    (package "java-common" :present)
    (package (<< "/tmp/~{deb}") :present)))
