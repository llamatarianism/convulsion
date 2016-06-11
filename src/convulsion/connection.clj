(ns convulsion.connection
  (:import (java.net Socket))
  (:require [convulsion.config :as conf]
            [clojure.java.io :as io]))

(defn- make-connection
  [settings]
  (let [socket (Socket. (:host settings) (:port settings))]
    {:socket socket
     :in     (io/reader socket)
     :out    (io/writer socket)}))

(def
  ^{:doc "The socket that the commands write and read from."}
  conn
  (make-connection conf/conn-settings))
