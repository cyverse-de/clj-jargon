(ns clj-jargon.by-uuid
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [throw+]]
            [clojure-commons.error-codes :as error]
            [clj-jargon.metadata :as meta])
  (:import [clojure.lang IPersistentMap IPersistentVector]
           [java.util UUID]
           [org.irods.jargon.core.pub IRODSGenQueryExecutor]
           [org.irods.jargon.core.query
            IRODSGenQueryBuilder
            IRODSQueryResultRow
            QueryConditionOperators
            RodsGenQueryEnum]))


(def ^String uuid-attr
  "This is the iRODS metadata attribute that holds the UUID."
  "ipc_UUID")


(defn ^String get-path
  "Returns the path of an entity given its UUID.

   Parameters:
     cm   - an open jargon context
     uuid - the UUID of the entity

   Returns:
     If found, it returns the path of the entity."
  [^IPersistentMap cm ^UUID uuid]
  (let [results (meta/list-everything-with-attr-value cm uuid-attr uuid)]
    (when-not (empty? results)
      (when (> (count results) 1)
        (log/error "Too many results for" uuid ":" (count results))
        (log/debug "Results for" uuid ":" results)
        (throw+ {:error_code error/ERR_TOO_MANY_RESULTS
                 :count      (count results)
                 :uuid       uuid}))
      (first results))))

(defn- build-file-uuid-query
  "Builds the general query for mapping multiple UUIDs to their respective file paths. UUIDs that do not refer to
   files will be excluded from the result."
  [uuids]
  (as-> (IRODSGenQueryBuilder. true nil) builder
    (.addSelectAsGenQueryValue builder RodsGenQueryEnum/COL_META_DATA_ATTR_VALUE)
    (.addSelectAsGenQueryValue builder RodsGenQueryEnum/COL_COLL_NAME)
    (.addSelectAsGenQueryValue builder RodsGenQueryEnum/COL_DATA_NAME)
    (.addConditionAsGenQueryField builder
                                  RodsGenQueryEnum/COL_META_DATA_ATTR_NAME
                                  QueryConditionOperators/EQUAL uuid-attr)
    (.addConditionAsMultiValueCondition builder
                                        RodsGenQueryEnum/COL_META_DATA_ATTR_VALUE
                                        QueryConditionOperators/IN
                                        (mapv str uuids))
    (.exportIRODSQueryFromBuilder builder meta/max-gen-query-results)))

(defn- build-collection-uuid-query
  "Builds the general query for mapping multiple UUIDs to their respective collection paths. UUIDs that do not refer
   to colletions will be excluded from the result."
  [uuids]
  (as-> (IRODSGenQueryBuilder. true nil) builder
    (.addSelectAsGenQueryValue builder RodsGenQueryEnum/COL_META_COLL_ATTR_VALUE)
    (.addSelectAsGenQueryValue builder RodsGenQueryEnum/COL_COLL_NAME)
    (.addConditionAsGenQueryField builder
                                  RodsGenQueryEnum/COL_META_COLL_ATTR_NAME
                                  QueryConditionOperators/EQUAL uuid-attr)
    (.addConditionAsMultiValueCondition builder
                                        RodsGenQueryEnum/COL_META_COLL_ATTR_VALUE
                                        QueryConditionOperators/IN
                                        (mapv str uuids))
    (.exportIRODSQueryFromBuilder builder meta/max-gen-query-results)))

(defn ^IPersistentMap get-paths
  "Returns the paths of multiple entities given their UUIDs.

   Parameters:
     cm    - an open jargon context
     uuids - a vector of UUIDs

   Returns: A map from UUID to the path of the associated entity. UUIDs that could not be found will be missing from the
   resulting map."
  [{^IRODSGenQueryExecutor executor :executor} ^IPersistentVector uuids]
  (let [partitions (partition-all 500 uuids)
        get-values (fn [^IRODSQueryResultRow row] (.getColumnsAsList row))
        format     (juxt first (comp (partial string/join "/") rest))
        run-query  (fn [build-query uuids]
                     (mapv (comp format get-values)
                           (.getResults (.executeIRODSQueryAndCloseResult executor (build-query uuids) 0))))]
    (into {} (concat (mapcat (partial run-query build-collection-uuid-query) partitions)
                     (mapcat (partial run-query build-file-uuid-query) partitions)))))
