(ns kotoba.ontology-test
  (:require [clojure.test :refer [deftest is testing]]
            [kotoba.ontology :as ontology]))

(deftest get-type-test
  (testing "seeded :tender type resolves"
    (is (= "Procurement Tender / Opportunity" (:name (ontology/get-type :tender)))))
  (testing "unknown id resolves to nil"
    (is (nil? (ontology/get-type :no-such-type)))))

(deftest conforms?-test
  (testing "fact with the required attribute conforms"
    (is (true? (ontology/conforms? :tender {:status :open}))))
  (testing "fact missing the required attribute does not conform"
    (is (false? (ontology/conforms? :tender {:agency "example"}))))
  (testing "unknown object type never conforms"
    (is (nil? (ontology/conforms? :no-such-type {:status :open})))))
