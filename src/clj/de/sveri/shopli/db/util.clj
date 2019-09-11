(ns de.sveri.shopli.db.util)

(defn uuid-str->pg-uuid  [uuid]
  (doto (org.postgresql.util.PGobject.)
    (.setType "uuid")
    (.setValue uuid)))
