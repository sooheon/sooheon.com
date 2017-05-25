(ns site.util
  (:require [clj-time.coerce :as to]
            [clj-time.format :as tf]))

(defn iso-date-fmt [date]
  (tf/unparse (tf/formatter "yyyy-MM-dd") (to/from-date date)))

(defn yyyy [date]
  (tf/unparse (tf/formatter "yyyy") (to/from-date date)))

(defn MM-dd [date]
  (tf/unparse (tf/formatter "MM-dd") (to/from-date date)))

(defn yyyy-MM-fmt [date]
  (tf/unparse (tf/formatter "yyyy/MM/") (to/from-date date)))

(defn month-fmt [date]
  (str (subs (tf/unparse (tf/formatter "MMMM") (to/from-date date)) 0 3)
       (tf/unparse (tf/formatter " yyyy") (to/from-date date))))

(defn trace [x] (prn x) x)
