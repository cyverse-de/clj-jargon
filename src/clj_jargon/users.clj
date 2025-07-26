(ns clj-jargon.users
  (:use [clj-jargon.validations]
        [clj-jargon.gen-query])
  (:import [org.irods.jargon.core.connection IRODSAccount]
           [org.irods.jargon.core.exception DataNotFoundException]
           [org.irods.jargon.core.query RodsGenQueryEnum]
           [org.irods.jargon.core.pub UserGroupAO
                                      UserAO]
           [org.irods.jargon.core.pub.domain User UserGroup]))

(def ^:private user-type-mapping
  {"rodsuser"   :user
   "groupadmin" :group-admin
   "rodsadmin"  :admin
   "rodsgroup"  :group
   "unknown"    :unknown
   })

(defn user
  [{^UserAO user-ao :userAO} username]
  (try
    (let [jargon-user (.findByName user-ao username)]
      {:id (.getId jargon-user)
       :type (get user-type-mapping (.getTextValue (.getUserType jargon-user)) :unknown)
       :name (.getName jargon-user)
       :zone (.getZone jargon-user)
       :info (.getInfo jargon-user)
       :comment (.getComment jargon-user)
       :date-created (long (.getTime (.getCreateTime jargon-user)))
       :date-modified (long (.getTime (.getModifyTime jargon-user)))})
    (catch DataNotFoundException _ {:type :none})))

(defn username->id
  [cm username]
  (:id (user cm username)))

(defn user-exists?
  "Returns true if 'username' exists in iRODS."
  [cm username]
  (not (= :none (:type (user cm username)))))

(defn user-groups
  "Returns a list of group names that the user is in."
  [{^UserGroupAO ug-ao :userGroupAO} username]
  (for [^UserGroup ug (.findUserGroupsForUser ug-ao username)]
    (.getUserGroupName ug)))

(defn user-group-ids
  "Returns a list of group IDs that the user is in."
  [{^UserGroupAO ug-ao :userGroupAO} username]
  (for [^UserGroup ug (.findUserGroupsForUser ug-ao username)]
    (.getUserGroupId ug)))

(defn proxied?
  "Returns true if this context map is using a proxied (client) user"
  [{:keys [^IRODSAccount irodsAccount]}]
  (cond (not (= (.getUserName irodsAccount) (.getProxyName irodsAccount))) true
        (not (= (.getZone irodsAccount) (.getProxyZone irodsAccount))) true
        :else false))

(defn group-exists?
  [{^UserGroupAO ug-ao :userGroupAO} group-name]
  (-> (.findByName ug-ao group-name)
      nil?
      not))

(defn list-group-members
  "List members of a group named `group-name` (qualified usernames)"
  [{^UserGroupAO ug-ao :userGroupAO} group-name]
  (for [^User u (.listUserGroupMembers ug-ao group-name)]
    (.getNameWithZone u)))

(defn create-user-group
  "Create a new user group named `group-name` in the logged-in user zone"
  [{^UserGroupAO ug-ao :userGroupAO zone :zone} group-name]
  (let [group (doto (new UserGroup)
                (.setUserGroupName group-name)
                (.setZone zone))]
      (.addUserGroup ug-ao group)))

(defn delete-user-group
  "Delete a group named `group-name` in the logged-in user zone"
  [{^UserGroupAO ug-ao :userGroupAO zone :zone} group-name]
  (let [group (doto (new UserGroup)
                (.setUserGroupName group-name)
                (.setZone zone))]
      (.removeUserGroup ug-ao group)))

(defn add-to-group
  "Add a user `username` to the group `group-name`"
  [{^UserGroupAO ug-ao :userGroupAO zone :zone} group-name username]
  (.addUserToGroup ug-ao group-name username zone))

(defn remove-from-group
  "Remove a user `username` from the group `group-name`"
  [{^UserGroupAO ug-ao :userGroupAO zone :zone} group-name username]
  (.removeUserFromGroup ug-ao group-name username zone))
