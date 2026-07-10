(ns kotoba.ontology.connector-test
  (:require [clojure.test :refer [deftest is testing]]
            [kotoba.ontology.connector :as connector]))

(deftest get-connector-test
  (testing "both real goyoukiki connectors resolve"
    (is (= :tender (:object-type (connector/get-connector :jp.kkj))))
    (is (= :tender (:object-type (connector/get-connector :jp.geps)))))
  (testing "an unregistered id resolves to nil"
    (is (nil? (connector/get-connector :not-a-real-connector)))))

(deftest tag-test
  (testing "tag stamps object type and source from the connector registry"
    (let [fact (connector/tag :jp.kkj {:status :open})]
      (is (= :tender (:ontology/type fact)))
      (is (= :jp.kkj (:ontology/source fact)))
      (is (not (contains? fact :ontology/fetched-at)))))
  (testing "optional fetched-at/confidence are attached only when given"
    (let [fact (connector/tag :jp.geps {:status :awarded} :fetched-at "2026-07-10" :confidence 0.9)]
      (is (= "2026-07-10" (:ontology/fetched-at fact)))
      (is (= 0.9 (:ontology/confidence fact)))))
  (testing "an unregistered connector-id fails closed rather than stamping :ontology/type nil"
    (is (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo :cljs cljs.core/ExceptionInfo)
                          #"unregistered connector"
                          (connector/tag :not-a-real-connector {:status :open})))))

(deftest tagged-conforms?-test
  (testing "a fact tagged by its own connector and conforming to the object type passes"
    (is (true? (connector/tagged-conforms? :jp.kkj (connector/tag :jp.kkj {:status :open})))))
  (testing "a fact missing a required attribute fails even if tagged"
    (is (false? (connector/tagged-conforms? :jp.kkj (connector/tag :jp.kkj {})))))
  (testing "a fact tagged by a different connector than claimed fails"
    (is (false? (connector/tagged-conforms? :jp.geps (connector/tag :jp.kkj {:status :open})))))
  (testing "an unregistered connector-id is simply false, not an error -- a caller checking an
            untrusted/unverified claim should never have to catch an exception to find out"
    (is (false? (connector/tagged-conforms? :not-a-real-connector {:status :open})))))
