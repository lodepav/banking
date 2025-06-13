package com.example.banking.repository

import com.example.banking.domain.Account
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class AccountRepositorySpec extends Specification {

    @Shared
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("banking")
            .withUsername("banking")
            .withPassword("banking")

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        postgres.start()
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
    }

    @Autowired
    AccountRepository accountRepository

    def setup() {
        accountRepository.deleteAll()
        accountRepository.saveAll([
                new Account(id: UUID.randomUUID(), clientId: "client-1", currency: "USD", balance: BigDecimal.ZERO),
                new Account(id: UUID.randomUUID(), clientId: "client-1", currency: "EUR", balance: BigDecimal.ZERO)
        ])
    }

    def "should find accounts by client ID"() {
        when:
        def accounts = accountRepository.findByClientId("client-1")

        then:
        accounts.size() == 2
        accounts*.currency.sort() == ["EUR", "USD"]
    }
}
