package com.skypro.simplebanking.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class SecurityConfigurationIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
            .withDatabaseName("banking")
            .withUsername("postgres")
            .withPassword("Wertyrwer1");

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {

    }
    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/account/123"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testAdminAccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/")
                        .content("{\"username\": \"newuser\", \"password\": \"password123\"}")
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(username = "user", roles = "USER")
    public void testUserAccessToNonExistentEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/non-existent-endpoint"))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(username = "user", roles = "USER")
    public void testTransferToNonexistentAccount() throws Exception {
        long sourceAccountId = 1L;
        long destinationAccountId = -2L;
        long transferAmount = 100L;

        mockMvc.perform(put("/account/{sourceAccountId}/transfer/{destinationAccountId}/{amount}", sourceAccountId, destinationAccountId, transferAmount))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(username = "user", roles = "USER")
    public void testTransferFromNonexistentAccount() throws Exception {
        long sourceAccountId = -1L;
        long destinationAccountId = 2L;
        long transferAmount = 100L;

        mockMvc.perform(put("/account/{sourceAccountId}/transfer/{destinationAccountId}/{amount}", sourceAccountId, destinationAccountId, transferAmount))
                .andExpect(status().isNotFound());
    }

}
