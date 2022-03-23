(defproject org.cyverse/clj-jargon "3.0.2"
  :description "Clojure API on top of iRODS's jargon-core."
  :url "https://github.com/cyverse-de/clj-jargon"
  :license {:name "BSD"
            :url "http://iplantcollaborative.org/sites/default/files/iPLANT-LICENSE.txt"}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :plugins [[jonase/eastwood "0.3.14"]
            [test2junit "1.2.2"]]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.logging "1.1.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.irods.jargon/jargon-core "4.2.2.1-RELEASE"
                  :exclusions [[org.jglobus/JGlobus-Core]
                               [org.slf4j/slf4j-api]
                               [org.slf4j/slf4j-log4j12]]]
                 [org.irods.jargon/jargon-data-utils "4.2.2.1-RELEASE"
                  :exclusions [[org.slf4j/slf4j-api]
                               [org.slf4j/slf4j-log4j12]]]
                 [org.irods.jargon/jargon-ticket "4.2.2.1-RELEASE"
                  :exclusions [[org.slf4j/slf4j-api]
                               [org.slf4j/slf4j-log4j12]]]
                 [cheshire "5.10.0"]
                 [medley "1.3.0"]
                 [slingshot "0.12.2"]
                 [org.cyverse/otel "0.2.4"]
                 [org.cyverse/clojure-commons "3.0.6"]]
  :profiles {:repl {:source-paths ["repl"]}}
  :eastwood {:exclude-linters [:unlimited-use]}
  :repositories [["cyverse-de"
                  {:url "https://raw.github.com/cyverse-de/mvn/master/releases"}]
                 ["dice.repository"
                  {:url "https://raw.github.com/DICE-UNC/DICE-Maven/master/releases"}]
                 ["renci-snapshot.repository"
                  {:url "https://ci-dev.renci.org/nexus/content/repositories/renci-snapshot/"}]])
