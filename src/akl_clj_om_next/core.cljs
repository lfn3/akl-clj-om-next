(ns akl-clj-om-next.core
  (:require
    [om.dom :as dom]
    [om.next :as om :refer-macros [defui]])
  (:require-macros
    [devcards.core :refer [defcard defcard-doc defcard-om-next om-next-root dom-node]]))

(defcard-doc
  "We're gonna start out with the most basic thing possible, a defui with just a simple element.
   Use dom/p, dom/div etc to add whatever you want inside the render method below,
   and you should see your (unique and beautiful) creation pop up below.

   The ```dom``` functions are used like ```(dom/div #js {:html-attr-name \"attr-value\"} child-nodes```

   You can put plain strings into child-nodes, and just use nil for the attrs: ```(dom/div nil \"text\")```")

(defui SimpleElement
  Object
  (render [this]))

(defcard-om-next your-element
                 SimpleElement)

(defcard-doc
  "You can also alter the css and it will hot load (due to ```:css-dirs``` in the figwheel config).

   Next, define an element that takes props. Use ```(om/props this)``` inside the render method,
   and pass whatever values you want inside the map used by the defcard. Use ```(:key (om/props this))```
   to extract values from the props.")

(defui WithProps
  Object
  (render [this]))

(defcard-om-next element-with-props
                 WithProps
                 {})

(defcard-doc
  "Thus far it's just been plain react stuff, but now it starts to get a bit more interesting.
   We're going to look at queries, which make the process of displaying stuff quite a bit more complicated.
   We won't see the payoff of this for a little while, but have faith.
   Note that defining a query won't actually change the way your component works:")

(defui WithAQuery
  static om/IQuery
  (query [this]
    [:this :query :does :not :change :anything])
  Object
  (render [this]
    (dom/div nil "Look, the query doesn't do anything!")))

(defcard-om-next defui-with-a-query
                 WithAQuery)

(defcard-doc
  "But if you modify the above component to include a call to ```(om/props)```, and try to use it
   in the same was as you did in the ```element-with-props``` card, you'll notice it doesn't work.
   Now we have to look at a bit of extra scaffolding to get this working, which you can see in the source below.")

(defmulti simple-read om/dispatch)

(defmethod simple-read :default
  [{:keys [_ state]} k _]
  (let [st @state]
    {:value (get st k)}))

(defn simple-reconciler [intial-state]
  (om/reconciler {:state  (atom intial-state)
                  :parser (om/parser {:read simple-read})}))

(defui WithAReconciler
  static om/IQuery
  (query [this]
    '[:message])
  Object
  (render [this]
    (dom/div nil
             "The message is: "
             (:message (om/props this)))))

(defcard-om-next elem-with-reconciler
                 WithAReconciler
                 (simple-reconciler {:message "Words!"}))

(defcard-doc
  "Note that the above is super simple. It doesn't really handle anything complicated, like nested queries.
   There's loads more details on queries than I'm going to go into in [Tony Kay's tutorial](https://github.com/awkay/om-tutorial).
   I'd highly recommend that, by the way. So now lets write some query related stuff to see how that behaves.
   Fix up the components below, so that they correctly render.")

(defui INeedAQuery
  static om/IQuery
  (query [this]
    []                                                      ;TODO: Fix this query
    )
  Object
  (render [this]
    (let [{:keys [some things]} (om/props this)]
      (dom/div nil
               (dom/h1 nil "I'm " some)
               (for [thing things]
                 (dom/p nil thing))))))

(defcard-om-next should-have-title-and-suessian-stuff
                 INeedAQuery
                 (simple-reconciler {:some "the title" :things ["one thing" "two thing" "red thing" "blue thing"]}))

(defui INeedSomeData
  static om/IQuery
  (query [this]
    [:a-simple-query :with-loads :of-keys])
  Object
  (render [this]
    (let [{:keys [a-simple-query with-loads of-keys]} (om/props this)]
      (dom/div nil "My query has three keys: "
             (dom/ul nil
                     (dom/li nil a-simple-query)
                     (dom/li nil "Also " with-loads)
                     (dom/li nil "And " of-keys))))))

(defcard-om-next feed-me-some-interesting-data
                 INeedSomeData
                 (simple-reconciler {}                        ;TODO: put some stuff in here?
                                    ))

(defcard-doc
  "Pretty simple, right? Like I said, you can get way more complicated. We're just going to cover a couple more things
   to do with queries though. First off, composition.

   This is why you co-locate your queries with your components, so that you can build up large queries without
   having to know the details of what data the children need. Note in this case the structure is flat, but this is just
   for the simple parsers benefit.")

(defui DemoChild
  static om/IQuery
  (query [this]
    [:child1 :child2])
  Object
  (render [this]
    (let [{:keys [child1 child2]} (om/props this)]
      (dom/div nil
               (dom/p nil child1)
               (dom/p nil child2)))))

(def demo-child (om/factory DemoChild))

(defui DemoParent
  static om/IQuery
  (query [this]
    `[:parent ~@(om/get-query DemoChild)])
  Object
  (render [this]
    (let [{:keys [parent] :as props} (om/props this)]
      (dom/div nil (dom/h1 nil parent)
               (demo-child props)))))

(defcard-om-next parent-and-child
                 DemoParent
                 (simple-reconciler {:parent "This is the parent"
                                     :child1 "Data passed to the child"
                                     :child2 "More data for the child!"}))

(defcard-doc
  "So if you mess with the query on the child and the data, you'll be able add additional data to the child
   without having to alter the parent at all. Give that a try.")

(defcard-doc
  "Now we're going to turn up the difficulty a little bit, by using a more complicated query parser,
   and adding idents to components. Take a look at the parser in source below.")

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [query state]} k _]
  (let [st @state]
    (prn st)
    {:value (om/db->tree query (get st k) st)}))

(defn make-reconciler [db]
  (om/reconciler {:state db
                  :parser (om/parser {:read read})}))

(defcard-doc
  "Doesn't look more complicated, but db->tree is like the laziest way to get your data from the form that om likes
   into the tree structure that your ui desires. The example below shows a component with an ident.")

(defui ComponentWithIdent
  static om/Ident
  (ident [_ {:keys [id]}]
    [:items/by-id id])
  static om/IQuery
  (query [this]
    [:id :text])
  Object
  (render [this]
    (let [{:keys [text]} (om/props this)]
      (dom/li nil text))))

(def component-with-ident (om/factory ComponentWithIdent))

(defui ParentComponent
  static om/IQuery
  (query [this]
    `[{:items ~(om/get-query ComponentWithIdent)}])
  Object
  (render [this]
    (let [{:keys [items]} (om/props this)]                  ;Note how we're still passing the props explictly.
      (dom/ul nil (map component-with-ident items)))))

(def denormalized-data {:items [{:id 1 :text "Item one"}
                                {:id 2 :text "Item two"}
                                {:id 3 :text "Item three"}]})

(def ident-reconciler (make-reconciler denormalized-data))

(defcard-om-next ident-example
                 ParentComponent
                 ident-reconciler)

(defcard-doc
  "In the read method above, slip a cheeky prn in, to convince yourself that the data gets changed from the denormalized form
  we pass in:")

(defcard denormed-data
         denormalized-data)

(defcard-doc
  "To something more like:")

(defcard normed-data
         {:items [[:items/by-id 1]
                  [:items/by-id 2]
                  [:items/by-id 3]]
          :items/by-id {1 {:id 1 :text "Item one"}
                        2 {:id 2 :text "Item two"}
                        3 {:id 3 :text "Item three"}}})

(defcard-doc
  "Ok, that's all I've got. Now it's up to you guys. What do you want to do now?
  Let's have a bit of talk about what to do now, or next time.")