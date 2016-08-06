(defproject funcool/codeina "0.5.0"
  :description "Generate documentation from Clojure source files"
  :url "https://github.com/funcool/codeina"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src"]
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  :repositories [["clojars" {:sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.93"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [leinjacker "0.4.2"]
                 [hiccup "1.0.5"]
                 [org.pegdown/pegdown "1.6.0"]]
  :plugins [[lein-ancient "0.6.10"]]
  :jar-exclusions [#"\.swp|\.swo|user.clj"]
  :eval-in-leiningen true)
