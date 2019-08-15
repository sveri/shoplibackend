(ns de.sveri.shopli.routes.websocket-test
  (:require [clojure.test :refer :all]
            [de.sveri.shopli.setup :refer [test-base-url setup-db server-setup]]
            [clj-http.client :as cl]
            [clojure.data.json :as j]
            [gniazdo.core :as g]
            [clojure.string :as str]))

(use-fixtures :each setup-db)
(use-fixtures :once server-setup)

(def ws-base-url (str/replace test-base-url "http://" "ws://"))

(defn get-api-token-response []
  (cl/post (str test-base-url "mobile/authenticate")
           {:body         (j/write-str {:device-id "device-id"
                                        :app-id    "app-id"})
            :content-type :json
            :throw-exceptions false}))

(defn get-api-token []
  (let [resp (get-api-token-response)
        body (j/read-str (:body resp) :key-fn keyword)]
    (-> body :api-token)))


(deftest open-websocket-without-auth
  (let [result (promise)]
    (g/connect (str ws-base-url "ws")
               :on-receive (fn [s] (deliver result (:status (j/read-str s :key-fn keyword)))))
    (is (= 403 (deref result 5000 0)))))

(deftest open-websocket-with-invalid-auth-token
  (let [result (promise)]
    (g/connect (str ws-base-url "ws")
               :on-receive (fn [s] (deliver result (:status (j/read-str s :key-fn keyword))))
               :headers {"Authorization" "Token falsetoken"})
    (is (= 403 (deref result 5000 0)))))

(deftest open-websocket-with-valid-auth-token
  (let [result (promise)]
    (g/connect (str ws-base-url "ws")
               ;:on-receive (fn [s] (println s))
               :on-receive (fn [s] (deliver result (:status (j/read-str s :key-fn keyword))))
               :headers {"Authorization" (get-api-token)})
               ;:headers {"Authorization" (str "Token " (get-api-token))})
    (is (= 200 (deref result 5000 0)))))

