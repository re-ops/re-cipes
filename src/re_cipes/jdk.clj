(ns re-cipes.jdk
  "Setting up the jdk"
  (:require
   [re-cipes.access :refer (permissions)]
   [clojure.core.strint :refer (<<)]
   [re-cog.resources.file :refer (file line)]
   [re-cog.resources.package :refer (package key-file update-)]
   [re-cog.resources.user :refer (group-add)]
   [re-cog.resources.download :refer (download)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} jdk-11
  "Setting up Amazon corretto 11 jdk"
  []
  (let
   [sources "/etc/apt/sources.list.d"
    listing (<< "~{sources}/corretto.list")
    keyrings "/usr/share/keyrings"
    url "https://apt.corretto.aws/corretto.key"
    key "corretto.key"
    repo "deb https://apt.corretto.aws stable main"]
    (download url (<< "~{keyrings}/~{key}") "506ca4fb064c77a6fafc0de072d5f89076e9e9eeab27b1a84c7f0b73de4934b7")
    (key-file (<< "~{keyrings}/~{key}"))
    (file listing :present)
    (line listing repo :present)
    (update-)
    (package "java-1.8.0-amazon-corretto-jdk:amd64" :absent)
    (package "java-11-amazon-corretto-jdk" :present)))
