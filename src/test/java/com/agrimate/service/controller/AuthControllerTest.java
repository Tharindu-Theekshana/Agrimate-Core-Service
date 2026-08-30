package com.agrimate.service.controller;

import com.agrimate.service.dto.AuthDtos.AuthResponse;
import com.agrimate.service.dto.UserDto;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.repository.UserRepository;
import com.agrimate.service.security.JwtAuthenticationFilter;
import com.agrimate.service.security.JwtService;
import com.agrimate.service.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@org.springframework.context.annotation.Import({
        com.agrimate.service.config.SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuthService authService;
    @MockitoBean private UserRepository userRepository;

    private UserDto sampleUser() {
        return new UserDto(1L, "kasun", "kasun@agrimate.lk", "Kasun Perera", null, null, null,
                RoleName.FARMER, java.util.List.of(RoleName.FARMER), RoleName.FARMER,
                com.agrimate.service.model.account.AgronomistStatus.NONE, null, false);
    }

    @Test
    void register_missingOtpCodeParam_returns400NotServerError() throws Exception {
        mockMvc.perform(multipart("/api/auth/register")
                        .param("username", "kasun")
                        .param("email", "kasun@agrimate.lk")
                        .param("password", "secret123")
                        .param("name", "Kasun Perera"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required parameter: code"));
    }

    @Test
    void register_validRequest_returns200WithTokens() throws Exception {
        when(authService.register(any(), anyString(), any()))
                .thenReturn(new AuthResponse("access-token", "refresh-token", sampleUser()));

        mockMvc.perform(multipart("/api/auth/register")
                        .param("username", "kasun")
                        .param("email", "kasun@agrimate.lk")
                        .param("password", "secret123")
                        .param("name", "Kasun Perera")
                        .param("code", "654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void register_serviceThrowsConflict_returns409() throws Exception {
        when(authService.register(any(), anyString(), any()))
                .thenThrow(ApiException.conflict("Username is already taken"));

        mockMvc.perform(multipart("/api/auth/register")
                        .param("username", "kasun")
                        .param("email", "kasun@agrimate.lk")
                        .param("password", "secret123")
                        .param("name", "Kasun Perera")
                        .param("code", "654321"))
                .andExpect(status().isConflict());
    }

    @Test
    void requestRegisterOtp_validEmail_returns204() throws Exception {
        mockMvc.perform(post("/api/auth/register/request-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"kasun\",\"email\":\"kasun@agrimate.lk\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void requestRegisterOtp_malformedEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register/request-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"kasun\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returns200WithTokens() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponse("access-token", "refresh-token", sampleUser()));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"kasun\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("kasun"));
    }

    @Test
    void login_serviceRejectsCredentials_returns400() throws Exception {
        when(authService.login(any())).thenThrow(ApiException.badRequest("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"kasun\",\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void passwordResetRequest_returns204RegardlessOfWhetherEmailExists() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"anyone@agrimate.lk\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void passwordResetConfirm_validCode_returns200WithFreshTokens() throws Exception {
        when(authService.confirmPasswordReset(anyString(), anyString(), anyString()))
                .thenReturn(new AuthResponse("new-access", "new-refresh", sampleUser()));

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"kasun@agrimate.lk\",\"code\":\"111222\",\"newPassword\":\"newSecret1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }
}
