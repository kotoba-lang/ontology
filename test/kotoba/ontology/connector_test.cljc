(ns kotoba.ontology.connector-test
  (:require [clojure.test :refer [deftest is testing]]
            [kotoba.ontology.connector :as connector]))

(deftest get-connector-test
  (testing "both real goyoukiki connectors resolve"
    (is (= :tender (:object-type (connector/get-connector :jp.kkj))))
    (is (= :tender (:object-type (connector/get-connector :jp.geps))))))

(deftest tag-test
  (testing "tag stamps object type and source from the connector registry"
    (let [fact (connector/tag :jp.kkj {:status :open})]
      (is (= :tender (:ontology/type fact)))
      (is (= :jp.kkj (:ontology/source fact)))
      (is (not (contains? fact :ontology/fetched-at)))))
  (testing "optional fetched-at/confidence are attached only when given"
    (let [fact (connector/tag :jp.geps {:status :awarded} :fetched-at "2026-07-10" :confidence 0.9)]
      (is (= "2026-07-10" (:ontology/fetched-at fact)))
      (is (= 0.9 (:ontology/confidence fact))))))

(deftest tagged-conforms?-test
  (testing "a fact tagged by its own connector and conforming to the object type passes"
    (is (true? (connector/tagged-conforms? :jp.kkj (connector/tag :jp.kkj {:status :open})))))
  (testing "a fact missing a required attribute fails even if tagged"
    (is (false? (connector/tagged-conforms? :jp.kkj (connector/tag :jp.kkj {})))))
  (testing "a fact tagged by a different connector than claimed fails"
    (is (false? (connector/tagged-conforms? :jp.geps (connector/tag :jp.kkj {:status :open}))))))
