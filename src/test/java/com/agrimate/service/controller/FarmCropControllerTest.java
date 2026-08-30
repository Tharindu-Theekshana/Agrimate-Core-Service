package com.agrimate.service.controller;

import com.agrimate.service.dto.FarmDtos.FarmDto;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.account.AgronomistStatus;
import com.agrimate.service.model.role.Role;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.userRole.UserRole;
import com.agrimate.service.repository.UserRepository;
import com.agrimate.service.security.JwtAuthenticationFilter;
import com.agrimate.service.security.JwtService;
import com.agrimate.service.service.CropService;
import com.agrimate.service.service.FarmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {FarmController.class, CropController.class})
@org.springframework.context.annotation.Import({
        com.agrimate.service.config.SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
class FarmCropControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockitoBean private FarmService farmService;
    @MockitoBean private CropService cropService;
    @MockitoBean private UserRepository userRepository;

    private User farmer;

    @BeforeEach
    void setUp() {
        farmer = new User();
        farmer.setId(1L);
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

    // BE-WEB-FARM-01
    @Test
    void listFarms_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/farms")).andExpect(status().isUnauthorized());
    }

    // BE-WEB-FARM-02
    @Test
    void listFarms_withValidToken_returns200() throws Exception {
        when(farmService.list(any())).thenReturn(List.of(
                new FarmDto(1L, "My Field", null, null, null, null)));

        mockMvc.perform(get("/api/farms").header("Authorization", bearer()))
                .andExpect(status().isOk());
    }

    // BE-WEB-FARM-03
    @Test
    void createFarm_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/api/farms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Field\"}"))
                .andExpect(status().isUnauthorized());
    }

    // BE-WEB-FARM-04
    @Test
    void addCropToFarm_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/api/farms/1/crops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
