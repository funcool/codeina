(ns leiningen.doc
  (:refer-clojure :exclude [doc])
  (:require [leinjacker.deps :as deps]
            [leinjacker.eval :as eval]
            [leiningen.core.project :as project]
            [clojure.string :as str]))

(defn- get-options [project]
  (merge {:sources (:source-paths project ["src"])}
         (-> project :codeina)
         {:name (str/capitalize (:name project))}
         (select-keys project [:root :version :description])
         (-> project :codeina :project)))

(defn doc
  "Generate API documentation from source code."
  [project]
  (let [project (if (get-in project [:profiles :codeina])
                  (project/merge-profiles project [:codeina])
                  project)]
    (eval/eval-in-project
     (deps/add-if-missing project '[funcool/codeina "0.5.0"])
     `(codeina.core/generate-docs
       (update-in '~(get-options project) [:src-uri-mapping] eval))
     `(require 'codeina.core))))
