package com.example.banking.repository


import net.bytebuddy.utility.dispatcher.JavaDispatcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.domain.Specification
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class AccountRepositorySpec extends Specification {

    @JavaDispatcher.Container
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