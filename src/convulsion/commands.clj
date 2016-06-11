(ns convulsion.commands
  (:require [convulsion.connection :as conn :refer [conn]]
            [convulsion.config :as conf]))

(defn write
  [& msg]
  (binding [*out* (:out conn)]
    (apply println msg)
    (flush)))

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
