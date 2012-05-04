(defproject clas "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [com.stuartsierra/lazytest "1.1.2"]
                 [ring/ring-core "0.3.8"]
                 [ring/ring-jetty-adapter "0.3.8"]
                 [org.danlarkin/clojure-json "1.1"]
                 [http.async.client "0.2.0"]
                 [clj-time "0.1.0-SNAPSHOT"]
                 [com.draines/postal "1.4.0-SNAPSHOT"]
                 [org.clojars.kjw/mysql-connector "5.1.11"]]
  :dev-dependencies
                 [[ring/ring-devel "0.3.8"]
		  [swank-clojure "1.2.1"]]
  :jvm-opts      ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8030"]
  :repositories {"stuartsierra-releases" "http://stuartsierra.com/maven2"}
  :source-path "src/:src/appmodule/:src/proxymodule/"
  :omit-source true
  :main com.bensmann.appsrv.server)
