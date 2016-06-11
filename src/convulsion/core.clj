(ns convulsion.core
  (:require [convulsion.commands :as comms]
            [convulsion.connection :as conn :refer [conn]]
            [convulsion.config :as conf]
            [clojure.core.async :as async :refer [thread thread-call go go-loop chan >! >!! <! <!!]]))

;(defn read-input
 ; []
  ;(binding [*in* (:in conn)]
   ; (go-loop []
    ;  (println (read-line))
                                        ; (recur))))
(def chan-echo (chan (async/sliding-buffer 1)))
(go-loop [] (println (<! chan-echo)) (recur))

(def chan-say (chan (async/sliding-buffer 1)))
(go-loop [] (comms/say "#llamatarianism" (<! chan-say)) (recur))

(defn -main [& args]
  (comms/authorise conf/user-settings)
  (comms/join "#llamatarianism")
  (comms/say "#llamatarianism" "oyeh")
  (thread
    (go-loop [ln (.readLine (:in conn))]
      (if-let [ping (re-find #"^PING (.+)" ln)]
        (comms/write "PONG " (second ping))
        (>! chan-echo ln))
      (recur (.readLine (:in conn)))))
  (loop []
    (let [ln (clojure.string/trim (read-line))]
      (when-not (#{":q" ":quit" ":exit"} ln)
        (if (= ":moo" ln)
          (println "you have unlocked SUPER COW POWERS!")
          (go (>! chan-say ln)))
        (recur)))))
