(ns codeina.core
  "Main namespace for generating documentation"
  (:require [codeina.utils :as utils]))

(defn- resolve-sym
  "Given a namespace qualified symbol, try resolve
  it and return the underlying value."
  [s]
  (let [ns-part (symbol (namespace s))]
    (require ns-part)
    (if-let [value (resolve s)]
      value
      (throw (Exception. (str "Could not resolve codeina writer " s))))))

(defn- macro? [var]
  (= (:type var) :macro))

(defn- read-macro-namespaces
  [& paths]
  (let [reader (resolve-sym 'codeina.reader.clojure/read-namespaces)]
    (->> (apply reader paths)
         (map (fn [ns] (update-in ns [:publics] #(filter macro? %))))
         (remove (comp empty? :publics)))))

(defn- merge-namespaces [namespaces]
  (for [[name nss] (group-by :name namespaces)]
    (assoc (first nss) :publics (mapcat :publics nss))))

(defn- cljs-read-namespaces
  [& paths]
  (let [reader (resolve-sym 'codeina.reader.clojurescript/read-namespaces)]
    (merge-namespaces
     (concat (apply reader paths)
             (apply read-macro-namespaces paths)))))

(defmulti get-writer
  "Get writer function."
  :writer)

(defmulti get-reader
  "Get reader function."
  :reader)

(defmethod get-writer :html5
  [options]
  (resolve-sym 'codeina.writer.html/write-docs))

(defmethod get-reader :clojure
  [options]
  (resolve-sym 'codeina.reader.clojure/read-namespaces))

(defmethod get-reader :clojurescript
  [options]
  cljs-read-namespaces)

(def ^:private
  +defaults+ {:target "doc/api"
              :format :markdown
              :sources ["src"]
              :root (System/getProperty "user.dir")
              :src-uri nil
              :src-uri-prefix nil
              :reader :clojure
              :writer :html5})

(defn generate-docs
  "Generate documentation from source files."
  ([]
   (generate-docs {}))
  ([options]
   (let [options (merge +defaults+ options)
         writer-fn (get-writer options)
         reader-fn (get-reader options)
         root (:root options)
         sources (seq (set (:sources options))) ;; Avoid reading twice
         include (:include options)
         exclude (:exclude options)
         namespaces (->> (apply reader-fn sources)
                         (utils/ns-filter include exclude)
                         (utils/add-source-paths root sources))]
     (writer-fn (assoc options :namespaces namespaces)))))
