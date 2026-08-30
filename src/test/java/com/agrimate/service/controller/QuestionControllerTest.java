package com.agrimate.service.controller;

import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.role.Role;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.userRole.UserRole;
import com.agrimate.service.repository.UserRepository;
import com.agrimate.service.security.JwtAuthenticationFilter;
import com.agrimate.service.security.JwtService;
import com.agrimate.service.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Covers: BE-WEB-QA-01..03 in the Test Cases document. */
@WebMvcTest(controllers = QuestionController.class)
@org.springframework.context.annotation.Import({
        com.agrimate.service.config.SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
class QuestionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockitoBean private QuestionService questionService;
    @MockitoBean private UserRepository userRepository;

    private User farmer;

    @BeforeEach
    void setUp() {
        farmer = new User();
        farmer.setId(1L);
        Account account = new Account();
        account.setId(1L);
        farmer.setAccount(account);
        farmer.getUserRoles().add(new UserRole(farmer, new Role(RoleName.FARMER, "Farmer")));
        when(userRepository.findDetailById(1L)).thenReturn(Optional.of(farmer));
    }

    // BE-WEB-QA-01
    @Test
    void listQuestions_withoutAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/questions")).andExpect(status().isUnauthorized());
    }

    // BE-WEB-QA-02
    @Test
    void answerQuestion_withoutAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(post("/api/questions/1/answers")
                        .contentType("application/json")
                        .content("{\"body\":\"Apply fungicide\"}"))
                .andExpect(status().isUnauthorized());
    }

    // BE-WEB-QA-03
    @Test
    void answerQuestion_authenticatedButMissingRequiredBodyField_returns400() throws Exception {
        mockMvc.perform(post("/api/questions/1/answers")
                        .header("Authorization", "Bearer " + jwtService.generateAccessToken(farmer))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
