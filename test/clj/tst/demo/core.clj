(ns tst.demo.core
  (:use tupelo.core
        tupelo.test)
  (:require
    [bidi.bidi :as bidi]
    ))

(dotest
  (let [route ["/index.html" :index]]
    (is= (bidi/match-route route "/index.html") {:handler :index}) ; found route
    (is= (bidi/match-route route "/another.html") nil) ; did not find route
    (is= "/index.html" (bidi/path-for route :index))) ; find URI for handler

  ; a map in the route tree indicates "branching" among multiple child routes
  (let [routes        ["/" ; common prefix
                       {"index.html"   :index
                        "article.html" :article}]
        routes-nested ["/" ; common prefix
                       {"index.html" :index
                        "articles/"  {"index.html"     :article-index
                                      "article-1.html" :article-1}}]]
    (is= (bidi/match-route routes "/index.html") {:handler :index})
    (is= (bidi/match-route routes-nested "/articles/index.html") {:handler :article-index})
    (is= (bidi/match-route routes-nested "/articles/article-1.html") {:handler :article-1})
    (is= (bidi/path-for routes-nested :article-1) "/articles/article-1.html"))

  ; how to retrieve path params
  (let [routes ["/" ; common prefix
                {"index.html" :index
                 "articles/"  {"article-index.html"     :article-index
                               [:art-id "/article.html"] :article-handler}}]]
    (is= (bidi/match-route routes "/articles/123/article.html")
      {:route-params {:art-id "123"}, :handler :article-handler})
    (is= (bidi/path-for routes :article-handler :art-id "123") "/articles/123/article.html"))

  ; short version to match GET "/index.html"
  (let [route ["/index.html" {:get :index}]]
    (is= (bidi/match-route route "/index.html" :request-method :get) {:handler :index, :request-method :get})
    (is= (bidi/match-route route "/index.html" :request-method :put) nil))

  ; long version to match GET "/index.html"
  (let [route ["" {
                   {:request-method :get} {"/index.html" :index}
                   }]]
    (is= (bidi/match-route route "/index.html" :request-method :get) {:handler :index, :request-method :get})
    (is= (bidi/match-route route "/index.html" :request-method :post) nil))

  ; An alternate long version to match GET "/index.html" (moved `/` to outer level)
  ; The route MUST be a vector, with a string as 1st item (could be empty string "")
  (let [route ["/" {
                    {:request-method :get} {"index.html" :index}
                    }]]
    (is= (bidi/match-route route "/index.html" :request-method :get) {:handler :index, :request-method :get})
    (is= (bidi/match-route route "/index.html" :request-method :post) nil))


  (let [route ["/" {"blog" {:get ; shortcut - :request-method as key
                            {"/index" :blog-index-handler}}
                    ; need clojure map as key for to specify more than one req
                    {:request-method :post :server-name "juxt.pro"}
                           {"zip" :post-zip-handler}}]]
    (is= (bidi/match-route route "/blog/index" :request-method :get)
      {:handler :blog-index-handler, :request-method :get})
    (is= (bidi/match-route route "/zip" :request-method :post :server-name "juxt.pro")
      {:server-name "juxt.pro", :handler :post-zip-handler, :request-method :post})
    (is= nil (bidi/match-route route "/zip" :request-method :get :server-name "juxt.pro")) ; method mismatch
    (is= nil (bidi/match-route route "/zip" :request-method :post :server-name "juxt.other")) ; server mismatch
    ))

