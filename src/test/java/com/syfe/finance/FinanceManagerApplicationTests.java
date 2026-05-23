package com.syfe.finance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FinanceManagerApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void completeFinanceFlowMatchesAssignmentExpectations() throws Exception {
        String username = "candidate" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "password", "password123",
                                "fullName", "Syfe Candidate",
                                "phoneNumber", "+1234567890"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").exists());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "password", "password123",
                                "fullName", "Duplicate Candidate",
                                "phoneNumber", "+1234567890"))))
                .andExpect(status().isConflict());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(get("/api/categories").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", hasSize(greaterThanOrEqualTo(7))));

        mockMvc.perform(post("/api/categories")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Freelance", "type", "INCOME"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Freelance"))
                .andExpect(jsonPath("$.isCustom").value(true));

        LocalDate transactionDate = LocalDate.now().minusDays(3);
        createTransaction(session, "3000.00", transactionDate, "Salary", "Monthly salary");
        createTransaction(session, "500.00", transactionDate, "Freelance", "Side project");
        createTransaction(session, "400.00", transactionDate, "Food", "Groceries");

        mockMvc.perform(get("/api/transactions")
                        .session(session)
                        .param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)));

        mockMvc.perform(get("/api/reports/monthly/" + transactionDate.getYear() + "/" + transactionDate.getMonthValue())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome.Salary").value(3000.00))
                .andExpect(jsonPath("$.totalExpenses.Food").value(400.00))
                .andExpect(jsonPath("$.netSavings").value(3100.00));

        MvcResult goal = mockMvc.perform(post("/api/goals")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "goalName", "Emergency Fund",
                                "targetAmount", "5000.00",
                                "targetDate", LocalDate.now().plusYears(1).toString(),
                                "startDate", transactionDate.minusDays(1).toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.currentProgress").value(3100.00))
                .andReturn();

        Integer goalId = objectMapper.readTree(goal.getResponse().getContentAsString()).get("id").asInt();
        mockMvc.perform(put("/api/goals/" + goalId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "targetAmount", "6000.00",
                                "targetDate", LocalDate.now().plusYears(2).toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingAmount").value(2900.00));

        mockMvc.perform(delete("/api/categories/Freelance").session(session))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void validationAndErrorScenariosReturnExpectedStatusCodes() throws Exception {
        String username = "errors" + System.nanoTime() + "@example.com";
        MockHttpSession session = registerAndLogin(username);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", "wrong-password"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/transactions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "amount", "100.00",
                                "date", LocalDate.now().plusDays(1).toString(),
                                "category", "Salary"))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/categories/Salary").session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reports/monthly/2024/13").session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transactionUpdatesDeletesAndCategoryDeletionRulesWork() throws Exception {
        MockHttpSession session = registerAndLogin("updates" + System.nanoTime() + "@example.com");
        LocalDate date = LocalDate.now().minusDays(1);

        mockMvc.perform(post("/api/categories")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Travel", "type", "EXPENSE"))))
                .andExpect(status().isCreated());

        MvcResult transactionResult = mockMvc.perform(post("/api/transactions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "amount", "800.00",
                                "date", date.toString(),
                                "category", "Travel",
                                "description", "Flight"))))
                .andExpect(status().isCreated())
                .andReturn();

        int transactionId = objectMapper.readTree(transactionResult.getResponse().getContentAsString()).get("id").asInt();
        mockMvc.perform(put("/api/transactions/" + transactionId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "amount", "900.00",
                                "category", "Food",
                                "description", "Updated expense",
                                "date", LocalDate.now().minusMonths(1).toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(900.00))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.date").value(date.toString()));

        mockMvc.perform(delete("/api/categories/Travel").session(session))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/transactions/" + transactionId).session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reports/monthly/" + date.getYear() + "/" + date.getMonthValue())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netSavings").value(0.00));
    }

    @Test
    void goalOwnershipIsolatedBetweenUsers() throws Exception {
        MockHttpSession firstUser = registerAndLogin("owner" + System.nanoTime() + "@example.com");
        MockHttpSession secondUser = registerAndLogin("intruder" + System.nanoTime() + "@example.com");

        MvcResult goalResult = mockMvc.perform(post("/api/goals")
                        .session(firstUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "goalName", "Laptop",
                                "targetAmount", "1200.00",
                                "targetDate", LocalDate.now().plusMonths(6).toString()))))
                .andExpect(status().isCreated())
                .andReturn();

        int goalId = objectMapper.readTree(goalResult.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(get("/api/goals/" + goalId).session(secondUser))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/goals/" + goalId).session(firstUser))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/goals/" + goalId).session(firstUser))
                .andExpect(status().isNotFound());
    }

    private void createTransaction(MockHttpSession session, String amount, LocalDate date,
                                   String category, String description) throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "amount", amount,
                                "date", date.toString(),
                                "category", category,
                                "description", description))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value(category));
    }

    private MockHttpSession registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "password", "password123",
                                "fullName", "Test User",
                                "phoneNumber", "+1234567890"))))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "password", "password123"))))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(session);
        return session;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
