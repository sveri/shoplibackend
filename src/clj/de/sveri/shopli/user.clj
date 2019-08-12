(ns de.sveri.shopli.user
  (:require [de.sveri.shopli.components.components :refer [dev-system]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [system.repl :refer [system set-init! start] :as sr]))

(defn startup []
  (set-init! #'dev-system)
  (start))

(defn reset []
  (sr/stop)
  (refresh)
  (start))
