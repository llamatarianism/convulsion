ns convulsion.commands
  (:require [convulsion.connection :as conn :refer [conn]]
            [convulsion.config :as conf]
            [clojure.core.async :as async :refer [chan go go-loop <! <!! >! >!!]]))

(defn write
  [msg]
  (binding [*out* (:out conn)]
    (println msg)
    (flush)))

(def chan-write (chan (async/sliding-buffer 10)))
(go-loop [] (write (<! chan-write)) (recur))

(defn join
  [chan]
  (>!! chan-write (str "JOIN " chan)))

(defn say
  [chan msg]
  (>!! chan-write (str "PRIVMSG " chan " :" msg)))

(defn authorise
  [user]
  (>!! chan-write (str "PASS " (:auth user)))
  (>!! chan-write (str "NICK " (:nick user))))
