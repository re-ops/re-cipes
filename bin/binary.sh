LEIN_SNAPSHOTS_IN_RELEASE=1 lein with-profile package do uberjar
cat bin/stub.sh target/re-cipes-0.3.14-standalone.jar > target/re-cipes && chmod +x target/re-cipes
