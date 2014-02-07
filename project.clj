(defproject rule110 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [domina "1.0.2"]
                 ]

  :main ^{:skip-aot true} rule110.core

  :plugins [[lein-cljsbuild "1.0.1"]]

  :cljsbuild {
              :builds [{:id "rule110"
                        :source-paths ["src"]
                        :compiler {
                                   :output-to "resources/public/rule110.js"
                                   :output-dir "resources/public/js/"
                                   :optimizations :none
                                   :source-map true}}]
              :crossovers [rule110.constants]
              }
  )
