(ns de.sveri.shopli.components.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes GET]]
            [noir.response :refer [redirect]]
            [noir.util.middleware :refer [app-handler]]
            [ring.middleware.defaults :refer [site-defaults]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.format :as rmf]
            [ring.util.response :as rur]
            [buddy.auth.middleware :as bam]
            ;[immutant.web.middleware :as web-middleware]
            [compojure.route :as route]
            [com.stuartsierra.component :as comp]
            [ring.middleware.reload :refer [wrap-reload]]
            [buddy.auth.accessrules :as baa]
            [de.sveri.shopli.routes.websockets :as rw]
            ;[de.sveri.shopli.routes.websockets :refer [websocket-callbacks]]
            [de.sveri.shopli.routes.mobile :as rm]
            [de.sveri.shopli.routes.home :refer [home-routes]]
            [de.sveri.shopli.routes.user :refer [user-routes registration-routes]]
            [de.sveri.shopli.middleware :refer [load-middleware]]
            [de.sveri.shopli.service.auth :as s-auth]))

(defroutes base-routes
           (route/resources "/")
           (route/not-found "Not Found"))

;; timeout sessions after 30 minutes
(def session-defaults
  {:timeout          (* 15 60 30)
   :timeout-response (redirect "/")})

(defn- mk-defaults
  "set to true to enable XSS protection"
  [xss-protection?]
  (-> site-defaults
      (update-in [:session] merge session-defaults)
      (assoc-in [:security :anti-forgery] xss-protection?)))

;(def handler
;  "Ring handler."
;  (-> (GET "/" [] (ring.response/content-type
;                    (ring.response/resource-response "index.html" {:root "/"})
;                    "text/html"))
;      ;(ring.resource/wrap-resource "")
;      wrap-reload
;      (web-middleware/wrap-session {:timeout 20})
;      (web-middleware/wrap-websocket websocket-callbacks)))



(defn get-handler [config locale {:keys [db]}]
  (routes
    (-> (rw/ws-routes config db))
        ;(wrap-routes baa/wrap-access-rules {:rules s-auth/ws-rules :on-error s-auth/my-unauthorized-handler})
        ;(wrap-routes bam/wrap-authorization s-auth/jws-backend)
        ;(wrap-routes bam/wrap-authentication s-auth/jws-backend))


    ;(rm/mobile-routes config db)
    (-> (rm/mobile-routes config db)
        (wrap-routes baa/wrap-access-rules {:rules s-auth/rest-rules :on-error s-auth/my-unauthorized-handler})
        (wrap-routes bam/wrap-authorization s-auth/jws-backend)
        (wrap-routes bam/wrap-authentication s-auth/jws-backend)
        (wrap-routes rmf/wrap-restful-format :formats [:json-kw]))

    (-> (app-handler
          (into [] (concat (when (:registration-allowed? config) [(registration-routes config db)])
                           ;; add your application routes here
                           [home-routes  (user-routes config db) base-routes]))
          ;; add custom middleware here
          :middleware (load-middleware config)
          :ring-defaults (mk-defaults false)
          ;; add access rules here
          :access-rules []
          ;; serialize/deserialize the following data formats
          ;; available formats:
          ;; :json :json-kw :yaml :yaml-kw :edn :yaml-in-html
          :formats [:json-kw :edn :transit-json])
        ; Makes static assets in $PROJECT_DIR/resources/public/ available.
        (wrap-file "resources")
        ; Content-Type, Content-Length, and Last Modified headers for files in body
        (wrap-file-info))))

    ;(-> (GET "/ws" [] (rur/content-type
    ;                    ;(rur/resource-response "index.html" {:root "/ws"})
    ;                    (rur/resource-response "index.html")
    ;                    "text/html"))
    ;    (web-middleware/wrap-websocket (websocket-callbacks config db)))))

(defrecord Handler [config locale db]
  comp/Lifecycle
  (start [comp]
    (assoc comp :handler (get-handler (:config config) locale db)))
  ;(assoc comp :handler handler))
  (stop [comp] comp))


(defn new-handler []
  (map->Handler {}))
