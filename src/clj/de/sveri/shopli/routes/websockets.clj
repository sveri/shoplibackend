(ns de.sveri.shopli.routes.websockets
  (:require [compojure.core :refer [routes GET POST]]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
    ;[immutant.web.async :as async]
            [buddy.auth.backends.token :as t]
            [buddy.sign.jwt :as bj]
            [org.httpkit.server :as hs]
            [clojure.data.json :as j]
            [buddy.sign.jwt :as jwt]
            [de.sveri.shopli.service.auth :as sa]))

(defn send-forbidden-message [channel]
  (hs/send! channel (j/write-str {:status 403 :message "Access Forbidden"})))

(defn close-because-unauthorized [channel]
  (send-forbidden-message channel)
  (hs/close channel))

(defn auth-token-valid? [auth-token]
  (try
    (jwt/unsign auth-token sa/secret {:alg :hs512})
    true
    (catch Exception _ false)))

(defn is-authorized-ws? [req channel]
  (let [auth-token (get-in req [:headers "authorization"])]
    (if (nil? auth-token)
      (close-because-unauthorized channel)
      (if (auth-token-valid? auth-token)
        (hs/send! channel (j/write-str {:status 200 :message "Channel Opened"}))
        (send-forbidden-message channel)))))

(defn on-open [request channel]
  ;(hs/send! channel (j/write-str {:status 202 :message "Channel Opened"})))
  (is-authorized-ws? request channel))

;(defn on-open [db]
;  "'onopen' websocket callback handler."
;  (fn [channel]
;    ;(fn [channel handshake]
;    (is-authorized-ws? channel)))
;
;
;(defn on-message [db]
;  "'onmessage' websocket callback handler."
;  (fn [channel message]
;    (clojure.pprint/pprint channel)
;    (println "Value received. Equal  : " message)
;    (async/send! channel "message received")
;    (if (< (.intValue (Integer/parseInt message)) 10)
;      (async/send! channel (inc (.intValue (Integer/parseInt message)))))))
;
;(defn on-close
;  "'onclose' websocket callback handler."
;  [channel {:keys [code reason]}]
;  (println "Channel closed because of reason: " reason "\nwith code: " code))
;
;(defn websocket-callbacks [config db]
;  "Websocket callback handler mapping."
;  {:on-open    (on-open db)
;   :on-message on-message
;   :on-close   on-close})

;(def websocket-callbacks
;  "Websocket callback handler mapping."
;  {:on-open    on-open
;   :on-message on-message
;   :on-close   on-close})

;(def callbacks
;  {:on-message (fn [ch msg]
;                 (async/send! ch (.toUpperCase msg)))})
;

(defonce channels (atom #{}))

;(defn connect! [channel]
;  (swap! channels conj channel)
;  (hs/send! channel "opened"))

(defn ws-handler [request]
  (hs/with-channel request channel
                (on-open request channel)))
                ;(on-open channel)))
                ;(on-close channel #(disconnect! channel %))
                ;(on-receive channel #(dispatch channel %))))

(defn ws-routes [config db]
  (routes
    (GET "/ws" req ws-handler)))
    ;(POST "/ws" req callbacks)))

