(ns de.sveri.shopli.routes.mobile
  (:require [compojure.core :refer [routes GET POST]]
            [compojure.coercions :refer [as-int]]
            [ring.util.response :refer [response status]]
            [de.sveri.shopli.db.lists :as db-l]
            [de.sveri.shopli.db.list-entry :as db-le]
            [de.sveri.shopli.db.mobile-clients :as db-mc]
            [de.sveri.shopli.service.auth :as sa]
            [buddy.sign.jwt :as jwt]
            [clojure.tools.logging :as log]))

(defn get-mobile-clients-id [db req]
  (let [claims (sa/get-claims-from-req req)]
    (:id (db-mc/get-or-create-client-by-app-device-id db (:device-id claims) (:app-id claims)))))

(defn authenticate [device-id app-id req]
  (response {:api-token (jwt/sign {:device-id device-id :app-id app-id} sa/secret {:alg :hs512})}))


(defn add-list [name db req]
  (try
    (let [mobile-clients-id (get-mobile-clients-id db req)
          list (db-l/create-list db name mobile-clients-id)]
      (response {:status :ok :list list}))
    (catch Exception e (log/error "Failed adding list with name: " name)
                       (.printStackTrace e)
                       (status (response {:error "failed adding list"}) 500))))


(defn get-lists [db req]
  (let [mobile-clients-id (get-mobile-clients-id db req)]
    (response {:status :ok :lists (db-l/get-lists db mobile-clients-id)})))

(defn add-list-entry [list-id name db]
  (try
    (let [list-entry (db-le/create-list-entry db name list-id)]
      (response {:status :ok :list-entry list-entry}))
    (catch Exception e (log/error "Failed adding list-entry with name: " name)
                       (.printStackTrace e)
                       (status (response {:error "failed adding list entry"}) 500))))

(defn get-list-entries [db list-id]
  (response {:status :ok :list-entries (db-le/get-list-entries db list-id)}))

(defn mobile-routes [config db]
  (routes
    (POST "/mobile/authenticate" [device-id app-id :as req] (authenticate device-id app-id req))
    (POST "/mobile/list" [name :as req] (add-list name db req))
    (GET "/mobile/list" req (get-lists db req))
    (POST "/mobile/list-entry" [list-id name] (add-list-entry list-id name db))
    (GET "/mobile/list-entry/list/:id" [id :<< as-int] (get-list-entries db id))))
