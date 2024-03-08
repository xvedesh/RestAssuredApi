Feature: As a User I should be able to retrieve clients data sending HTTP requests

  @ClientData
  Scenario Outline: As a User I should be able to retrieve client's data
    Given I send POST request to get Authorization Token
    Then I send POST request to create new "<block>" data
    Then I send GET request to "<block>" data service, and validate created data
    Then I send PUT request to update  crated "<block>" data and validate updated data
    Then I send PATCH request to update created "<block>" data and validate updated data
    Then I send DELETE request to delete created "<block>" data validate data removal

    Examples:
      | block       |
      | client      |
      | account     |
      | portfolio   |
      | transaction |

