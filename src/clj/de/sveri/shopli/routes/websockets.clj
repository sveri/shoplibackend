(ns de.sveri.shopli.routes.websockets
  (:require [compojure.core :refer [routes GET POST]]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [immutant.web.async :as async]))

(defn on-open [db]
  "'onopen' websocket callback handler."
  (fn [channel]
    (println "Channed opened.")))

(defn on-message [db]
  "'onmessage' websocket callback handler."
  (fn [channel message]
    (clojure.pprint/pprint channel)
    (println "Value received. Equal  : " message)
    (async/send! channel "message received")
    (if (< (.intValue (Integer/parseInt message)) 10)
      (async/send! channel (inc (.intValue (Integer/parseInt message)))))))

(defn on-close
  "'onclose' websocket callback handler."
  [channel {:keys [code reason]}]
  (println "Channel closed because of reason: " reason "\nwith code: " code))

(defn websocket-callbacks [config db]
  "Websocket callback handler mapping."
  {:on-open    (on-open db)
   :on-message on-message
   :on-close   on-close})

;(def websocket-callbacks
;  "Websocket callback handler mapping."
;  {:on-open    on-open
;   :on-message on-message
;   :on-close   on-close})

;(def callbacks
;  {:on-message (fn [ch msg]
;                 (async/send! ch (.toUpperCase msg)))})
;
;(defn ws-routes [config db]
;  (routes
;    (GET "/ws" req websocket-callbacks)))
;    ;(POST "/ws" req callbacks)))

