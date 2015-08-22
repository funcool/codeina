(defproject funcool/codeina "0.3.0"
  :description "Generate documentation from Clojure source files"
  :url "https://github.com/funcool/codeina"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src"]
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  :repositories [["clojars" {:sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.107"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [leinjacker "0.4.2"]
                 [hiccup "1.0.5"]
                 [org.pegdown/pegdown "1.4.2"]]
  :jar-exclusions [#"\.swp|\.swo|user.clj"]
  :eval-in-leiningen true)
