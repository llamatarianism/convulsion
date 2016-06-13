(ns convulsion.ircio
  (:require [clojure.core.async :as async :refer [>! >!! <! <!! go go-loop chan sliding-buffer]]
            [convulsion.commands :as comms]
            [convulsion.connection :as conn :refer [conn]]
            [clojure.string :as strn :refer [lower-case trim]]))

(def chan-echo (chan (sliding-buffer 10)))
(go-loop [] (println (<! chan-echo)) (recur))

(defn- say-print
  [ln]
  (let [[_ user chan mesg] (re-find #":(.+)!\1@\1\.tmi\.twitch\.tv PRIVMSG (#.+) :(.+)" ln)]
    (str "<" chan "> " user ": " mesg)))

(defn- join-print
  [ln]
  (let [[_ user chan] (re-find #":(.+)!\1@\1\.tmi\.twitch\.tv JOIN (#.+)" ln)]
    (str user " joined " chan)))

(defn- server-print
  [ln]
  (let [[_ num mesg] (re-find #":tmi\.twitch\.tv (\d{3}) .+ :(.+)" ln)]
    (str "<" num "> SERVER: " mesg)))

(defn pretty-print
  [ln]
  (condp re-find ln
    #":(.+)!\1@\1\.tmi\.twitch\.tv PRIVMSG (#.+) :(.+)" (say-print ln)
    #":(.+)!\1@\1\.tmi\.twitch\.tv JOIN (#.+)"          (join-print ln)
    #":tmi\.twitch\.tv (\d{3}) .+ :(.+)"                (server-print ln)
    ln))

(defn user-input-handler
  []
  (loop [ln (trim (read-line))]
    (when-not (#{":q" ":quit" ":exit"} ln)
      (if-let [[_ new-channel] (re-find #"^JOIN (#.+)" ln)]
        (comms/join new-channel)
        (let [[_ channel message] (re-find #"(.+)-> (.+)" ln)]
          (comms/say (str "#" channel) message)))
      (recur (trim (read-line))))))

(defn chat-input-handler
  []
  (go-loop [ln (.readLine (:in conn))]
    (if-let [[_ server] (re-find #"^PING (.+)" ln)]
      (>! comms/chan-write (str "PONG " server))
      (>! chan-echo (pretty-print ln)))
    (recur (.readLine (:in conn)))))


