(ns makejack.file-changed.api
  "Track file changes by hash.
  Hashes are checked if the file modification time changes."
  (:require
   [clojure.edn :as edn]
   [clojure.set :as set]
   [makejack.file-changed.impl :as impl]
   [makejack.filesystem.api :as fs]
   [makejack.path.api :as path]))

(defn load-file-info
  "Load a file info map from a path."
  [{:keys [file-path]}]
  (when (fs/file-exists? file-path)
    (edn/read-string (slurp (path/as-file file-path)))))

(defn save-file-info
  "Save a file info map from a path."
  [{:keys [file-path]} file-info]
  (spit (path/as-file file-path) (pr-str file-info)))

(defn changed-files
  "Return a file-info map a list of changed and removed paths.

  The returned map has :file-info, :changed, and :removed keys.
  New files are added to the file-info map. "
  [file-info paths]
  (reduce
   (fn [{:keys [file-info] :as result} path]
     (let [entry (impl/changed-file-hash file-info path)]
       (-> result
           (update :file-info into entry)
           (update :changed conj path))))
   {:file-info file-info
    :changed   #{}
    :removed   (set/difference (set (keys file-info)) (set paths))}
   paths))
