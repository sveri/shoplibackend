(ns de.sveri.shopli.setup
  (:require [com.stuartsierra.component :as component]
    ;[taoensso.tempura :refer [tr]]
            [clojure.test :refer :all]
            [org.httpkit.client :as cl]
            [system.repl :refer [go stop] :as repl]
            [clojure.java.jdbc :as j]
            [de.sveri.shopli.components.server :refer [new-web-server]]
            [de.sveri.shopli.components.handler :refer [new-handler]]
            [de.sveri.shopli.components.config :as c]
            [de.sveri.shopli.components.db :refer [new-db]])
  ;[de.sveri.shopli.locale :as l])
  (:import (java.util.logging Logger Level)))

(def db-uri "jdbc:postgresql://localhost:5432/shoplibackend_test?user=shoplibackend&password=shoplibackend")
(def db {:connection-uri db-uri})

(def ^:dynamic *driver*)

; custom config for test configuration
(def test-config
  {:hostname              "http://localhost/"
   :jdbc-url              db-uri
   :env                   :dev
   :registration-allowed? true
   :captcha-enabled?      false
   :captcha-public-key    "your public captcha key"
   :private-recaptcha-key "your private captcha key"
   :recaptcha-domain      "yourdomain"
   :jwt-secret            "kdfjasoiajwefkxjvoijawoiejroiwerjowierj"
   :port                  3001})


(defn test-system []
  (component/system-map
    :config (c/new-config test-config)
    :db (component/using (new-db) [:config])
    :handler (component/using (new-handler) [:config :db])
    :web (component/using (new-web-server) [:handler :config])))

(def test-base-url (str "http://localhost:3001/"))

(defn start-server []
  (repl/set-init! #'test-system)
  (go))

(defn stop-server []
  (stop))

(defn server-setup [f]
  (start-server)
  (f)
  (stop-server))

(defn setup-db [f]
  (j/execute! db ["CREATE EXTENSION IF NOT EXISTS \"pgcrypto\""])
  (j/execute! db ["drop table if exists mobile_clients;"])
  (j/execute! db ["CREATE TABLE mobile_clients ( id uuid primary key default gen_random_uuid(), device_id text NOT NULL, app_id
  text NOT NULL, user_id BIGINT);"])
  (j/execute! db ["drop table if exists lists;"])
  (j/execute! db ["CREATE TABLE lists (id uuid primary key default gen_random_uuid(), name text,mobile_clients_id
                  uuid NOT NULL);"])
  (j/execute! db ["drop table if exists list_entry;"])
  (j/execute! db ["CREATE TABLE list_entry (id uuid primary key default gen_random_uuid(), list_id uuid NOT NULL,
                    name text,done boolean default false not null,
                    created_at timestamp without time zone default (now()));"])
  (j/execute! db ["drop table if exists share_hashes;"])
  (j/execute! db ["CREATE TABLE share_hashes (mobile_clients_id uuid NOT NULL, hash TEXT NOT NULL, list_id uuid NOT NULL);"])
  (j/execute! db ["drop table if exists shared_list_to_user;"])
  (j/execute! db ["CREATE TABLE shared_list_to_user ( mobile_clients_id uuid NOT NULL, list_id uuid NOT NULL\n);"])
  (f))
