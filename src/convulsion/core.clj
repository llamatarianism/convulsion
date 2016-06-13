(ns convulsion.core
  (:require [convulsion.commands :as comms]
            [convulsion.connection :as conn]
            [convulsion.config :as conf]
            [convulsion.ircio :as io]
            [clojure.core.async :as async]))

(defn -main [& args]
  (let [channel (or (first args) (loop [channel nil]
                                   (if (empty? channel)
                                     (do (println "Please enter the name of a channel to connect to.")
                                         (recur (read-line)))
                                     channel)))]
    (comms/authorise conf/user-settings)
    (comms/join channel)
    (async/thread-call io/chat-input-handler)
    (io/user-input-handler channel)))
