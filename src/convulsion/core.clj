(ns convulsion.core
  (:require [convulsion.commands :as comms]
            [convulsion.connection :as conn]
            [convulsion.config :as conf]
            [convulsion.ircio :as io]
            [clojure.core.async :as async]))

(defn- repeated-prompt
  [s]
  (loop [x nil]
    (if (nil? x)
      (do (println "ENTER" s ">")
          (flush)
          (recur (read-line)))
      x)))

(defn -main [& args]
  (let [channel (or (:chan conf/user-settings) (repeated-prompt "CHANNEL"))
        auth    (or (:auth conf/user-settings) (repeated-prompt "OAUTH"))]
    (comms/authorise (assoc conf/user-settings :auth auth))
    (comms/join channel)
    (async/thread-call io/chat-input-handler)
    (io/user-input-handler channel)))
