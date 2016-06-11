(ns convulsion.core
  (:import (java.net Socket))
  (:require [clojure.core.async :as async :refer [<! <!! >! >!!]]
            [clojure.java.io :as io]))

(def ^:dynamic *conn* nil)

(def ^:const conn-settings
  {:host "irc.chat.twitch.tv"
   :port 6667})

(def ^:const user-settings
  {:nick "llamatarianism"
   :auth #_no-thanks})

(defn make-connection
  [settings]
  (let [socket (Socket. (:host settings) (:port settings))]
    {:socket socket
     :in     (io/reader socket)
     :out    (io/writer socket)}))

(defn write
  [& msg]
  (binding [*out* (:out *conn*)]
    (apply println msg)))

(defn join
  [chan]
  (write "JOIN " chan))

(defn say
  [chan msg]
  (write "PRIVMSG " chan " :" msg))

(defn authorise
  [user]
  (write "PASS " (:auth user))
  (write "NICK " (:nick user)))

(defn -main [& args]
  (let [conn (make-connection conn-settings)]
    (binding [*conn* conn]
      (authorise user-settings)
      (join "#henley")
      (say "#henley" "another testy test test"))))
