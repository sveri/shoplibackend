(ns de.sveri.shopli.routes.mobile
  (:require [compojure.core :refer [routes GET POST]]
            [compojure.coercions :refer [as-int]]
            [ring.util.response :refer [response status]]
            [de.sveri.shopli.db.lists :as db-l]
            [de.sveri.shopli.db.list-entry :as db-le]
            [de.sveri.shopli.db.mobile-clients :as db-mc]
            [de.sveri.shopli.service.auth :as sa]
            [buddy.sign.jwt :as jwt]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))

(defn get-mobile-clients-id [db req]
  (let [claims (sa/get-claims-from-req req)]
    (:id (db-mc/get-or-create-client-by-app-device-id db (:device-id claims) (:app-id claims)))))

(defn authenticate [device-id app-id req]
  (if (or (str/blank? device-id) (str/blank? app-id))
    (status (response {:error "device id and app id should not be empty"}) 500)
    (response {:api-token (jwt/sign {:device-id device-id :app-id app-id} sa/secret {:alg :hs512})})))


(defn add-list [name db req]
  (if (str/blank? name)
    (status (response {:error "name cannot be empty"}) 500)
    (try
      (let [mobile-clients-id (get-mobile-clients-id db req)
            list (db-l/create-list db name mobile-clients-id)]
        (response {:status :ok :list list}))
      (catch Exception e (log/error "Failed adding list with name: " name)
                         (.printStackTrace e)
                         (status (response {:error "failed adding list"}) 500)))))


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

(defn split-list-entries-and-add-to-list [list-entries-with-list]
  (vals (loop [list-entries-with-list list-entries-with-list lists-map {}]
          (if (empty? list-entries-with-list)
            lists-map
            (let [list-entry-with-list (first list-entries-with-list)
                  list-entry (select-keys list-entry-with-list [:id :name :done :created_at])
                  existing-list (get lists-map (:l_id list-entry-with-list))
                  existing-list-entries (get existing-list :list-entries)
                  list (assoc {} :id (:l_id list-entry-with-list) :name (:l_name list-entry-with-list)
                                 :list-entries (conj existing-list-entries list-entry))]
              (recur (rest list-entries-with-list) (assoc lists-map (:id list) list)))))))

(defn get-initial-data [db req]
  (let [mobile-clients-id (get-mobile-clients-id db req)
        list-entries-with-list (db-l/get-lists-with-entries db mobile-clients-id)
        lists (split-list-entries-and-add-to-list list-entries-with-list)]
    (response {:status :ok :lists lists})))


(defn mobile-routes [config db]
  (routes
    (GET "/mobile/initial-data" req (get-initial-data db req))
    (POST "/mobile/authenticate" [device-id app-id :as req] (authenticate device-id app-id req))
    (POST "/mobile/list" [name :as req] (add-list name db req))
    (GET "/mobile/list" req (get-lists db req))
    (POST "/mobile/list-entry" [list-id name] (add-list-entry list-id name db))
    (GET "/mobile/list-entry/list/:id" [id :<< as-int] (get-list-entries db id))))



(def l [{:l_id 1,
         :l_name "list-name",
         :id 1,
         :list_id 1,
         :name "entry-name1",
         :done false,
         :created_at "2019-08-19T06:28:26Z"}
        {:l_id 1,
         :l_name "list-name",
         :id 2,
         :list_id 1,
         :name "entry-name2",
         :done false,
         :created_at "2019-08-19T06:28:26Z"}
        {:l_id 2,
         :l_name "list-name2",
         :id 2,
         :list_id 1,
         :name "entry-name2",
         :done false,
         :created_at "2019-08-19T06:28:26Z"}])