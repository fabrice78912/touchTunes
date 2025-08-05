Feature: Récupération des play requests
  Afin de consulter les play requests
  En tant que client de l’API
  Je veux récupérer la liste paginée des play requests

  Scenario: Récupérer toutes les play requests sans filtre
    Given qu'il existe des play requests en base
    When j'appelle l'endpoint GET "/playrequests" sans filtre
    Then la réponse HTTP doit être 200
    And la réponse contient 1 play request

  Scenario: Récupérer les play requests avec un filtre "PENDING"
    Given qu'il existe des play requests avec le statut "PENDING"
    When j'appelle l'endpoint GET "/playrequests?filter=PENDING"
    Then la réponse HTTP doit être 200
    And la réponse contient des play requests avec le statut "PENDING"

  Scenario: Récupérer les play requests avec plusieurs filtres "PENDING,BOOSTED"
    Given qu'il existe des play requests avec les statuts "PENDING","BOOSTED"
    When j'appelle l'endpoint GET "/playrequests?filter=PENDING,BOOSTED"
    Then la réponse HTTP doit être 200
    And la réponse contient des play requests filtrées par "PENDING","BOOSTED"
