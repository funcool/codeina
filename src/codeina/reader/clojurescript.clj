(ns codeina.reader.clojurescript
  "Read raw documentation information from ClojureScript source directory."
  (:require [clojure.java.io :as io]
            [cljs.analyzer :as ana]
            [cljs.analyzer.api :as an]
            [cljs.env :as env]
            [clojure.string :as str]
            [codeina.utils :refer (assoc-some update-some correct-indent)]))

(defn- cljs-file?
  [file]
  (and (.isFile file)
       (or (-> file .getName (.endsWith ".cljs"))
           (-> file .getName (.endsWith ".cljc")))))

(defn- strip-parent
  [parent]
  (let [len (inc (count (.getPath parent)))]
    (fn [child]
      (let [child-name (.getPath child)]
        (when (>= (count child-name) len)
          (io/file (subs child-name len)))))))

(defn- find-files
  [file]
  (if (.isDirectory file)
    (->> (file-seq file)
         (filter cljs-file?)
         (keep (strip-parent file)))))

(defn- no-doc?
  [var]
  (or (:skip-wiki var)
      (:no-doc var)))

(defn- protocol-methods
  [protocol vars]
  (let [proto-name (name (:name protocol))]
    (filter #(if-let [p (:protocol %)] (= proto-name (name p))) vars)))

(defn- var-type
  [opts]
  (cond
    (:macro opts)           :macro
    (:protocol-symbol opts) :protocol
    :else                   :var))

(defn- read-var
  [file vars var]
  (-> var
      (select-keys [:name :line :arglists :doc :dynamic :added :deprecated :doc/format])
      (update-some :doc correct-indent)
      (update-some :arglists #(if (= 'quote (first %)) (second %) %))
      (assoc-some  :file    (cond
                              (instance? java.io.File file) (.getPath file)
                              (string? file) file)
                   :type    (var-type var)
                   :members (map (partial read-var file vars)
                                 (protocol-methods var vars)))))

(defn- namespace-vars
  [analysis namespace]
  (->> (:defs analysis)
       (map (fn [[name opts]] (assoc opts :name name)))))

(defn- read-publics
  [analysis namespace file]
  (let [vars (namespace-vars analysis namespace)]
    (->> vars
         (remove :protocol)
         (remove :anonymous)
         (remove no-doc?)
         (map (partial read-var file vars))
         (sort-by (comp str/lower-case :name)))))

(defn- analyze-file
  "Takes a file and returns then analysis map corresponding to its namespace"
  [file]
  (binding [ana/*analyze-deps* false]
    (env/with-compiler-env (an/empty-env)
      (an/no-warn
       (an/analyze-file file) ;; side-effects
       (an/find-ns (:ns (an/parse-ns file)))))))

(defn- read-file
  [path file]
  (try
    (let [ns-analysis (analyze-file (io/file path file))
          ns-name (:name ns-analysis)]
      {ns-name (-> ns-analysis
                   (assoc :name ns-name)
                   (assoc :publics (read-publics ns-analysis ns-analysis file))
                   (update-some :doc correct-indent)
                   (dissoc :use-macros :excludes :requires :imports :uses :defs
                           :require-macros ::an/constants))})
    (catch Exception e
      (println
       (format "Could not generate clojurescript documentation for %s - root cause: %s %s"
               file
               (.getName (class e))
               (.getMessage e))))))

(defn read-namespaces
  "Read ClojureScript namespaces from a source directory (defaults to
  \"src\"), and return a list of maps suitable for documentation
  purposes.

  The keys in the maps are:
    :name   - the name of the namespace
    :doc    - the doc-string on the namespace
    :author - the author of the namespace
    :publics - a collection of vars, each containing:
      :name       - the name of a public function, macro, or value
      :file       - the file the var was declared in
      :line       - the line at which the var was declared
      :arglists   - the arguments the function or macro takes
      :doc        - the doc-string of the var
      :type       - one of :macro, :protocol or :var
      :added      - the library version the var was added in
      :deprecated - the library version the var was deprecated in"
  ([] (read-namespaces "src"))
  ([path]
   (let [path (io/file path)
         file-reader (partial read-file path)]
     (->> (find-files path)
          (map file-reader)
          (apply merge)
          (vals)
          (sort-by :name))))
  ([path & paths]
   (mapcat read-namespaces (cons path paths))))
