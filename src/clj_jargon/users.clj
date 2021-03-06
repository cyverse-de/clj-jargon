(ns clj-jargon.users
  (:use [clj-jargon.validations]
        [clj-jargon.gen-query])
  (:require [otel.otel :as otel])
  (:import [org.irods.jargon.core.connection IRODSAccount]
           [org.irods.jargon.core.exception DataNotFoundException]
           [org.irods.jargon.core.query RodsGenQueryEnum]
           [org.irods.jargon.core.pub UserGroupAO
                                      UserAO]
           [org.irods.jargon.core.pub.domain UserGroup]))

(defn user
  [{^UserAO user-ao :userAO} username]
  (otel/with-span [s ["user" {:attributes {"user" username}}]]
    (try
      (let [jargon-user (.findByName user-ao username)]
        {:id (.getId jargon-user)
         :type :user ;; better if we use the UserTypeEnum for rodsuser/groupadmin/rodsadmin/rodsgroup/unknown
         :name (.getName jargon-user)
         :zone (.getZone jargon-user)
         :info (.getInfo jargon-user)
         :comment (.getComment jargon-user)
         :date-created (long (.getTime (.getCreateTime jargon-user)))
         :date-modified (long (.getTime (.getModifyTime jargon-user)))})
      (catch DataNotFoundException _ {:type :none}))))

(defn username->id
  [cm username]
  (otel/with-span [s ["username->id" {:attributes {"user" username}}]]
    (:id (user cm username))))

(defn user-exists?
  "Returns true if 'username' exists in iRODS."
  [cm username]
  (otel/with-span [s ["user-exists?" {:attributes {"user" username}}]]
    (not (= :none (:type (user cm username))))))

(defn user-groups
  "Returns a list of group names that the user is in."
  [{^UserGroupAO ug-ao :userGroupAO} username]
  (otel/with-span [s ["user-groups" {:attributes {"user" username}}]]
    (for [^UserGroup ug (.findUserGroupsForUser ug-ao username)]
      (.getUserGroupName ug))))

(defn user-group-ids
  "Returns a list of group IDs that the user is in."
  [{^UserGroupAO ug-ao :userGroupAO} username]
  (otel/with-span [s ["user-group-ids" {:attributes {"user" username}}]]
    (for [^UserGroup ug (.findUserGroupsForUser ug-ao username)]
      (.getUserGroupId ug))))

(defn proxied?
  "Returns true if this context map is using a proxied (client) user"
  [{:keys [^IRODSAccount irodsAccount]}]
  (cond (not (= (.getUserName irodsAccount) (.getProxyName irodsAccount))) true
        (not (= (.getZone irodsAccount) (.getProxyZone irodsAccount))) true
        :else false))
