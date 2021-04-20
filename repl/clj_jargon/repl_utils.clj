(ns clj-jargon.repl-utils
  (:require [cheshire.core :as json]
            [clj-jargon.init :as init]
            [clojure.java.io :as io]))

(def rods-conf-dir (io/file (System/getProperty "user.home") ".irods"))
(def prod-jargon-config (io/file rods-conf-dir ".prod-jargon.json"))
(def qa-jargon-config (io/file rods-conf-dir ".qa-jargon.json"))

(defn- load-config [path]
  (json/decode-stream (io/reader path) true))

(defn init [path]
  (let [{:keys [host zone port user password home resource max-retries retry-sleep use-trash]} (load-config path)]
    (init/init host port user password home zone resource
               :max-retries max-retries
               :retry-sleep retry-sleep
               :use-trash use-trash)))

(defn init-prod [] (init prod-jargon-config))
(defn init-qa [] (init qa-jargon-config))
