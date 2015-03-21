(defproject funcool/codox "0.1.0-SNAPSHOT"
  :description "Generate documentation from Clojure source files"
  :url "https://github.com/niwibe/codox"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src"]
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.namespace "0.2.10"]
                 [org.clojure/clojurescript "0.0-3126"]
                 [leinjacker "0.4.2"]
                 [hiccup "1.0.5"]
                 [org.pegdown/pegdown "1.4.2"]]

  :jar-exclusions [#"\.swp|\.swo|user.clj"]
  :eval-in-leiningen true)
