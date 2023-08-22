package com.skypro.simplebanking.controller;

import com.skypro.simplebanking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers

public class UserControllerIntegrationTest {

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

    @Autowired
    private UserRepository userRepository;


    @Test
    public void adminCanCreateNewUser() throws Exception {
        String adminToken = "SUPER_SECRET_KEY_FROM_ADMIN";

        String newUserJson = "{\"username\": \"newuser1\", \"password\": \"password123\"}";

        ResultActions resultActions = mockMvc.perform(post("/user")
                .header("X-SECURITY-ADMIN-KEY", adminToken)
                .content(newUserJson)
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void userCannotCreateNewUserWithoutToken() throws Exception {
        String newUserJson = "{\"username\": \"newuser\", \"password\": \"password123\"}";
        ResultActions resultActions = mockMvc.perform(post("/user")
                .content(newUserJson)
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void userCannotCreateAccountWithoutToken() throws Exception {
        String newAccountJson = "{\"currency\": \"USD\"}";

        ResultActions resultActions = mockMvc.perform(post("/account")
                .content(newAccountJson)
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void newUserMustHaveUsername() throws Exception {
        String adminToken = "SUPER_SECRET_KEY_FROM_ADMIN";

        String newUserJson = "{\"password\": \"strongpassword\"}";

        ResultActions resultActions = mockMvc.perform(post("/user")
                .header("X-SECURITY-ADMIN-KEY", adminToken)
                .content(newUserJson)
                .contentType(MediaType.APPLICATION_JSON));

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value((String) null))
                .andExpect(jsonPath("$.accounts").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Simulate user with ADMIN role
    public void testCreateUserWithAdminRole() throws Exception {
        String newUserJson = "{\"username\": \"newuser\", \"password\": \"password123\"}";

        mockMvc.perform(post("/user")
                        .content(newUserJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    public void userCannotGetListOfUsersWithoutToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/user"));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void unauthorizedAccessToCreateUser() throws Exception {
        String newUserJson = "{\"username\": \"newuser\", \"password\": \"password123\"}";

        ResultActions resultActions = mockMvc.perform(post("/user")
                .content(newUserJson)
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void unauthorizedAccessToGetListOfUsers() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/user"));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void unauthorizedAccessToDeleteUser() throws Exception {
        ResultActions resultActions = mockMvc.perform(delete("/user/{userId}", 1));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void userCannotUpdateUser() throws Exception {
        String updateUserJson = "{\"username\": \"updateduser\", \"password\": \"newpassword\"}";

        ResultActions resultActions = mockMvc.perform(put("/user/{userId}", 1)
                .content(updateUserJson)
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnauthorized());
    }
    @Test
    public void testUnauthorizedUserCreation() throws Exception {
        String newUserJson = "{\"username\": \"newuser1\", \"password\": \"password123\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .content(newUserJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

}

