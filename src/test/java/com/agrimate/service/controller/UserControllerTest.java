package com.agrimate.service.controller;

import com.agrimate.service.dto.UserDto;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.account.AgronomistStatus;
import com.agrimate.service.model.role.Role;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.userRole.UserRole;
import com.agrimate.service.repository.UserRepository;
import com.agrimate.service.security.JwtAuthenticationFilter;
import com.agrimate.service.security.JwtService;
import com.agrimate.service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = UserController.class)
@org.springframework.context.annotation.Import({
        com.agrimate.service.config.SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockitoBean private UserService userService;
    @MockitoBean private UserRepository userRepository;

    private User farmer;

    @BeforeEach
    void setUp() {
        farmer = new User();
        farmer.setId(1L);
        farmer.setUsername("kasun");
        Account account = new Account();
        account.setId(1L);
        account.setAgronomistStatus(AgronomistStatus.NONE);
        farmer.setAccount(account);
        farmer.getUserRoles().add(new UserRole(farmer, new Role(RoleName.FARMER, "Farmer")));
        when(userRepository.findDetailById(1L)).thenReturn(Optional.of(farmer));
    }

    private String bearer() {
        return "Bearer " + jwtService.generateAccessToken(farmer);
    }

    // BE-WEB-USR-01
    @Test
    void me_withoutAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    // BE-WEB-USR-02
    @Test
    void me_withValidToken_returns200WithTheCallersProfile() throws Exception {
        when(userService.me(1L)).thenReturn(new UserDto(1L, "kasun", "kasun@agrimate.lk", "Kasun Perera",
                null, null, null, RoleName.FARMER, List.of(RoleName.FARMER), RoleName.FARMER,
                AgronomistStatus.NONE, null, false));

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("kasun"));
    }

    // BE-WEB-USR-03
    @Test
    void updateMe_withoutAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .contentType("application/json")
                        .content("{\"name\":\"New Name\"}"))
                .andExpect(status().isUnauthorized());
    }

    // BE-WEB-USR-04
    @Test
    void registerDeviceToken_withoutAToken_returns400ValidationError() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/users/me/device-token")
                        .header("Authorization", bearer())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
