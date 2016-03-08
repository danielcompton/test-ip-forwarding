(ns clojure-getting-started.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [ring.middleware.proxy-headers :as proxy]))

(defn splash []
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "Hello from Heroku"})

(defn get-ip [req]
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    (str {:remote-addr     (:remote-addr req)
                  :x-forwarded-for (get-in req [:headers "x-forwarded-for"])})})

(defroutes app-routes
  (GET "/" []
    (splash))
  (GET "/ip" [] get-ip)
  (ANY "*" []
    (route/not-found (slurp (io/resource "404.html")))))

(def app
  (-> app-routes
      (proxy/wrap-forwarded-remote-addr)))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
