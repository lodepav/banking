package com.example.banking.repository


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification


@SpringBootTest
@ActiveProfiles("test")
class AccountRepositorySpec extends Specification {


    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine")

    @Autowired
    AccountRepository accountRepository

    def "should find accounts by client ID"() {
        given: "Test client with accounts"
        def clientId = "client-1"

        when: "Fetching accounts"
        def accounts = accountRepository.findByClientId(clientId)

        then: "Should return 2 accounts"
        accounts.size() == 2
        accounts*.currency.sort() == ["EUR", "USD"]
    }
}