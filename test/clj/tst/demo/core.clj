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
    (is= (bidi/path-for routes-nested :article-1) "/articles/article-1.html") )


  (let [route ["/index.html" {:get :index}]]
    (is= (bidi/match-route route "/index.html" :request-method :get) {:handler :index, :request-method :get})
    (is= (bidi/match-route route "/index.html" :request-method :put) nil)
    )

  )





