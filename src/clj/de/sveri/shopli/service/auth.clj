(ns de.sveri.shopli.service.auth
  (:require [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth :as ba]
            [ring.util.response :refer [redirect]]
            [buddy.auth.backends.token :as babt]
            [ring.util.response :as rur]
            [buddy.core.nonce :as nonce]))

(def ^:const available-roles ["admin" "none"])

(defn admin-access [req] (= "admin" (-> req :session :noir :role)))
(defn loggedin-access [req] (some? (-> req :session :noir :identity)))
(defn unauthorized-access [_] true)
(defn rest-loggedin-access [req] (ba/authenticated? req))

(def rules [{:pattern #"^/admin.*"
             :handler admin-access}
            {:pattern #"^/user/changepassword"
             :handler loggedin-access}
            {:pattern #"^/user.*"
             :handler unauthorized-access}
            {:pattern #"^/"
             :handler unauthorized-access}])

(def rest-rules
  [{:pattern #"^/mobile/authenticate.*"
    :handler unauthorized-access}
   {:pattern #"^/mobile/.*"
    :handler rest-loggedin-access}])

(defn unauthorized-handler
  [request _]
  (let [current-url (:uri request)]
    (redirect (format "/user/login?nexturl=%s" current-url))))

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))



(defn my-unauthorized-handler
  [_ _]
  (-> (rur/response "Unauthorized request")
      (assoc :status 403)))

(def secret (nonce/random-bytes 32))
(def jws-backend (babt/jws-backend {:secret secret :options {:alg :hs512}}))
;:unauthorized-handler my-unauthorized-handler}))

(defn get-claims-from-req [req]
  (:identity req))