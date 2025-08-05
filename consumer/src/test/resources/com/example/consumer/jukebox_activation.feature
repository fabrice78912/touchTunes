Feature: Activation d'un jukebox

  Scenario: Activation réussie
    Given le numéro de série "SN-123"
    When j'active le jukebox
    Then la réponse contient un identifiant de jukebox

  Scenario: Jukebox introuvable (404)
    Given le numéro de série "SN-404"
    When j'active le jukebox
    Then la réponse ne contient pas d'identifiant de jukebox
