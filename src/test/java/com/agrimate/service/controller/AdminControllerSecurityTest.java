package com.agrimate.service.controller;

import com.agrimate.service.dto.AdminDtos.CreateAdminRequest;
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
import com.agrimate.service.service.AdminService;
import com.agrimate.service.service.NewsService;
import com.agrimate.service.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@org.springframework.context.annotation.Import({
        com.agrimate.service.config.SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
class AdminControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockitoBean private AdminService adminService;
    @MockitoBean private NewsService newsService;
    @MockitoBean private StorageService storageService;
    @MockitoBean private UserRepository userRepository;

    @Value("${agrimate.jwt.secret}")
    private String jwtSecret;

    private User admin;
    private User farmer;

    @BeforeEach
    void setUp() {
        admin = userWithRole(1L, RoleName.ADMIN);
        farmer = userWithRole(2L, RoleName.FARMER);
        when(userRepository.findDetailById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findDetailById(2L)).thenReturn(Optional.of(farmer));
    }

    private User userWithRole(Long id, RoleName roleName) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@agrimate.lk");
        user.setPassword("hashed");
        Account account = new Account();
        account.setUser(user);
        account.setName("User " + id);
        account.setAgronomistStatus(AgronomistStatus.NONE);
        user.setAccount(account);
        Role role = new Role(roleName, roleName.name());
        user.getUserRoles().add(new UserRole(user, role));
        return user;
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateAccessToken(user);
    }

    @Test
    void createAdmin_withoutAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateAdminJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAdmin_withFarmerRoleToken_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", bearer(farmer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateAdminJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAdmin_withAdminRoleToken_returns201() throws Exception {
        when(adminService.createAdmin(any(CreateAdminRequest.class))).thenReturn(
                new UserDto(9L, "newadmin", "newadmin@agrimate.lk", "New Admin", null, null, null,
                        RoleName.ADMIN, java.util.List.of(RoleName.ADMIN), RoleName.ADMIN,
                        AgronomistStatus.NONE, null, false));

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateAdminJson()))
                .andExpect(status().isCreated());
    }

    @Test
    void listUsers_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void createAdmin_withMalformedToken_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer not-a-real-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateAdminJson()))
                .andExpect(status().isUnauthorized());
    }

    private String validCreateAdminJson() {
        return "{\"username\":\"newadmin\",\"email\":\"newadmin@agrimate.lk\",\"name\":\"New Admin\"}";
    }
}
