(ns tst.demo.core
  (:use tupelo.core tupelo.test)
  (:require
    [bidi.bidi :as bidi] ))

(dotest
  (let [route ["/index.html" :index]]
    (is= (bidi/match-route route "/index.html") ; found route
      {:handler :index})
    (is= (bidi/match-route route "/another.html") ; non-existant route => nil
      nil)
    (is= (bidi/path-for route :index) ; find URI for handler
      "/index.html"))

  ; a map in the route tree indicates "branching" among multiple child routes
  (let [routes   ["/" ; common prefix
                  {"index.html"   :index
                   "article.html" :article}]
        routes-2 ["/" ; common prefix
                  {""             :index ; add a default route so "/" and "/index.html" both => `:index` handler
                   "index.html"   :index
                   "article.html" :article}]]

    (is= (bidi/match-route routes "/index.html")
      {:handler :index})

    (is= nil (bidi/match-route routes "/")) ; plain "/" route is not available, has no default
    (is= nil (bidi/match-route routes-2 {:handler :index}))) ; Now we get the :index route

  ; a map in the route tree indicates "branching" among multiple child routes
  (let [routes        ["/" ; common prefix
                       {"index.html"   :index
                        "article.html" :article}]
        routes-nested ["/" ; common prefix
                       {"index.html" :index
                        "articles/"  {"index.html"     :article-index
                                      "article-1.html" :article-1}}]]

    (is= nil (bidi/match-route routes "/")) ; plain "/" route is not available, has no default

    (is= (bidi/match-route routes "/index.html")
      {:handler :index})
    (is= (bidi/match-route routes-nested "/articles/index.html")
      {:handler :article-index})
    (is= (bidi/match-route routes-nested "/articles/article-1.html")
      {:handler :article-1})
    (is= (bidi/path-for routes-nested :article-1)
      "/articles/article-1.html"))

  ; how to retrieve path params
  (let [routes ["/" ; common prefix
                {"index.html" :index
                 "articles/"  {"article-index.html"      :article-index
                               [:art-id "/article.html"] :article-handler}}]]
    (is= (bidi/match-route routes "/articles/123/article.html") ; lookup route with a path-param
      {:route-params {:art-id "123"}, :handler :article-handler})
    (is= (bidi/path-for routes :article-handler :art-id "123") ; find the route given the handler & a path-param value
      "/articles/123/article.html"))

  ; short version to match GET "/index.html"
  (let [route ["/index.html" {:get :index}]]
    (is= (bidi/match-route route "/index.html" :request-method :get) ; correct method yields expected result
      {:handler :index, :request-method :get})
    (is= (bidi/match-route route "/index.html" :request-method :put) ; fails for incorrect method
      nil))

  ; long version to match GET "/index.html"
  (let [route ["" {
                   {:request-method :get} {"/index.html" :index}
                   }]]
    (is= (bidi/match-route route "/index.html" :request-method :get) ; correct method yields expected result
      {:handler :index, :request-method :get})
    (is= (bidi/match-route route "/index.html" :request-method :post) ; fails for incorrect method
      nil))

  ; An alternate long version to match GET "/index.html" (moved `/` to outer level)
  ; The route MUST be a vector, with a string as 1st item (could be empty string "")
  (let [route ["/" {
                    {:request-method :get} {"index.html" :index}
                    }]]
    (is= (bidi/match-route route "/index.html" :request-method :get) ; correct method yields expected result
      {:handler :index, :request-method :get})
    (is= (bidi/match-route route "/index.html" :request-method :post) ; fails for incorrect method
      nil))


  (let [route ["/" {"blog" {:get ; shortcut - :request-method as key
                            {"/index" :blog-index-handler}}
                    {:request-method :post :server-name "juxt.pro"} ; need clojure map as key for to specify more than one req
                           {"zip" :post-zip-handler}}]]
    (is= (bidi/match-route route "/blog/index" :request-method :get) ; match route & method
      {:handler :blog-index-handler, :request-method :get})
    (is= (bidi/match-route route "/zip" :request-method :post :server-name "juxt.pro") ; match route, method, and :server-name
      {:server-name "juxt.pro", :handler :post-zip-handler, :request-method :post})
    (is= nil (bidi/match-route route "/zip" :request-method :get :server-name "juxt.pro")) ; method mismatch => fail to match
    (is= nil (bidi/match-route route "/zip" :request-method :post :server-name "juxt.other")) ; server mismatch => fail to match
    ))









