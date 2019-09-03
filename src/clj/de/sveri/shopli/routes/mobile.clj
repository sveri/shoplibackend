(ns de.sveri.shopli.routes.mobile
  (:require [compojure.core :refer [routes GET POST PUT]]
            [compojure.coercions :refer [as-uuid]]
            [ring.util.response :refer [response status]]
            [de.sveri.shopli.db.lists :as db-l]
            [de.sveri.shopli.db.list-entry :as db-le]
            [de.sveri.shopli.db.mobile-clients :as db-mc]
            [de.sveri.shopli.db.share_hash :as db-sh]
            [de.sveri.shopli.db.shared-list-to-user :as db-sltu]
            [de.sveri.shopli.service.auth :as sa]
            [buddy.sign.jwt :as jwt]
            [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:import (java.util UUID)))

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

(defn get-own-and-shared-lists [db mobile-clients-id]
  (let [lists (db-l/get-lists db mobile-clients-id)
        lists (mapv #(assoc % :shared false) lists)
        shared-lists (db-sltu/get-lists-by-client db mobile-clients-id)
        shared-lists (mapv #(assoc % :shared true) shared-lists)]
    (concat lists shared-lists)))

(defn get-lists [db req]
  (let [mobile-clients-id (get-mobile-clients-id db req)]
    (response {:status :ok :lists (get-own-and-shared-lists db mobile-clients-id)})))

(defn get-initial-data [db req]
  (let [mobile-clients-id (get-mobile-clients-id db req)
        lists (get-own-and-shared-lists db mobile-clients-id)
        lists-with-entries (map #(assoc % :list-entries (db-le/get-list-entries db (:id %))) lists)]
    (response {:status :ok :lists lists-with-entries})))

(defn add-list-entry [id list-id name db]
  (try
    (let [id (if (str/blank? id) (str (UUID/randomUUID)) id)
          ;names (mapv str/trim (str/split name #","))
          ;list-entry (mapv #(db-le/create-list-entry db id % list-id) names)
          list-entry (db-le/create-list-entry db id name list-id)]
      (response {:status :ok :list-entry list-entry}))
    (catch Exception e (log/error "Failed adding list-entry with name: " name)
                       (.printStackTrace e)
                       (status (response {:error "failed adding list entry"}) 500))))

(defn get-list-entries [db list-id]
  (response {:status :ok :list-entries (db-le/get-list-entries db list-id)}))

;(defn split-list-entries-and-add-to-list [list-entries-with-list]
;  (vals (loop [list-entries-with-list list-entries-with-list lists-map {}]
;          (if (empty? list-entries-with-list)
;            lists-map
;            (let [list-entry-with-list (first list-entries-with-list)
;                  list-entry (select-keys list-entry-with-list [:id :name :done :created_at])
;                  existing-list (get lists-map (:l_id list-entry-with-list))
;                  existing-list-entries (get existing-list :list-entries)
;                  list (assoc {} :id (:l_id list-entry-with-list) :name (:l_name list-entry-with-list)
;                                 :list-entries (conj existing-list-entries list-entry))]
;              (recur (rest list-entries-with-list) (assoc lists-map (:id list) list)))))))


(defn update-list-entry [id name done db]
  (try
    (db-le/update-list-entry db id name done)
    (response {:status :ok :list-entry (db-le/get-list-by-id db id)})
    (catch Exception e (log/error "Failed updating list-entry with name: " name)
                       (.printStackTrace e)
                       (status (response {:error "failed updating list entry"}) 500))))

(defn share-list [db list-id from to req]
  (try
    (let [mobile-clients-id (get-mobile-clients-id db req)
          uuid (UUID/randomUUID)
          _ (db-sh/create-hash db mobile-clients-id list-id from to uuid)]
      (response {:status :ok :hash uuid :from from :to to}))
    (catch Exception e (log/error "Failed to share a list: " name)
                       (.printStackTrace e)
                       (status (response {:error "failed to share a list"}) 500))))


(defn accept-share-list [db share-hash req]
  (let [mobile-clients-id (get-mobile-clients-id db req)
        share-hash (db-sh/get-hash db share-hash)
        ;shared-list-id (:list_id share-hash)
        _ (db-sltu/create-entry db mobile-clients-id share-hash)]
    (response {:status :ok})))


(defn mobile-routes [config db]
  (routes
    (GET "/mobile/initial-data" req (get-initial-data db req))
    (GET "/mobile/list" req (get-lists db req))
    (GET "/mobile/list-entry/list/:id" [id :<< as-uuid] (get-list-entries db id))
    (POST "/mobile/authenticate" [device-id app-id :as req] (authenticate device-id app-id req))
    (POST "/mobile/list" [name :as req] (add-list name db req))
    (POST "/mobile/list-entry" [id list-id name] (add-list-entry id list-id name db))
    (POST "/mobile/list/share" [list-id :<< as-uuid from to :as req] (share-list db list-id from to req))
    (POST "/mobile/list/accept-share" [share-hash :<< as-uuid :as req] (accept-share-list db share-hash req))
    (PUT "/mobile/list-entry/:id" [id :<< as-uuid name done] (update-list-entry id name done db))))

