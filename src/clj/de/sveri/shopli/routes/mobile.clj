(ns de.sveri.shopli.routes.mobile
  (:require [compojure.core :refer [routes GET POST]]
            [ring.util.response :refer [response status]]
            [de.sveri.shopli.db.lists :as db-l]
            [de.sveri.shopli.db.mobile-clients :as db-mc]
            [de.sveri.shopli.service.auth :as sa]
            [buddy.sign.jwt :as jwt]
            [clojure.tools.logging :as log]))

(defn authenticate [device-id app-id req]
  (response {:api-token (jwt/sign {:device-id device-id :app-id app-id} sa/secret {:alg :hs512})}))


(defn add-list [name db req]
  ;(clojure.pprint/pprint req)
  (try
    (let [claims (sa/get-claims-from-req req)
          mobile-clients-id (:id (db-mc/get-or-create-client-by-app-device-id db (:device-id claims) (:app-id claims)))
          list (db-l/create-list db name mobile-clients-id)]
      (response {:status :ok :list list}))
    (catch Exception e (log/error "Failed adding list with name: " name)
                       (.printStackTrace e)
                       (status (response {:error "failed adding list"}) 500))))


(defn mobile-routes [config db]
  (routes
    (POST "/mobile/authenticate" [device-id app-id :as req] (authenticate device-id app-id req))
    (POST "/mobile/add-list" [name :as req] (add-list name db req))))
