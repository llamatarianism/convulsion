(ns convulsion.core
  (:require [convulsion.commands :as comms :refer [chan-write]]
            [convulsion.connection :as conn :refer [conn]]
            [convulsion.config :as conf]
            [clojure.core.async :as async :refer [thread thread-call go go-loop chan >! >!! <! <!!]]))

(def channel (first *command-line-args*))

(def chan-echo (chan (async/sliding-buffer 10)))
(go-loop [] (println (<! chan-echo)) (recur))

(defn -main [& args]
  (comms/authorise conf/user-settings)
  (comms/join channel)
  (println (.readLine (:in conn)))
  (thread
    (go-loop [ln (.readLine (:in conn))]
      (if-let [ping (re-find #"^PING (.+)" ln)]
        (>! chan-write (str "PONG " (second ping)))
        (>! chan-echo ln))
      (recur (.readLine (:in conn)))))
  (loop []
    (let [ln (clojure.string/trim (read-line))]
      (when-not (#{":q" ":quit" ":exit"} ln)
        (if (= ":moo" ln)
          (println "you have unlocked SUPER COW POWERS!")
          (comms/say channel ln))
        (recur)))))
