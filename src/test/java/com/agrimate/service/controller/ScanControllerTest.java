package com.agrimate.service.controller;

import com.agrimate.service.dto.PredictionDto;
import com.agrimate.service.dto.ScanDto;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.role.Role;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.userRole.UserRole;
import com.agrimate.service.repository.UserRepository;
import com.agrimate.service.security.JwtAuthenticationFilter;
import com.agrimate.service.security.JwtService;
import com.agrimate.service.service.ScanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ScanController.class)
@org.springframework.context.annotation.Import({
        com.agrimate.service.config.SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
class ScanControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockitoBean private ScanService scanService;
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
    }

    // BE-WEB-SCAN-01
    @Test
    void guestScan_withNoAuthorizationHeader_returns200() throws Exception {
        when(scanService.scanGuest(any())).thenReturn(new ScanDto(
                null, null, "healthy", 0.9, List.of(new PredictionDto("healthy", 0.9)),
                null, false, true, null, null, null, null, null));
        var image = new MockMultipartFile("image", "leaf.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/scans/guest").file(image))
                .andExpect(status().isOk());
    }

    // BE-WEB-SCAN-02
    @Test
    void authenticatedScan_withoutAuthorizationHeader_returns401() throws Exception {
        var image = new MockMultipartFile("image", "leaf.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/scans").file(image))
                .andExpect(status().isUnauthorized());
    }

    // BE-WEB-SCAN-03
    @Test
    void authenticatedScan_withValidToken_returns200() throws Exception {
        when(userRepository.findDetailById(1L)).thenReturn(Optional.of(farmer));
        when(scanService.scan(any(), any(), any(), any(), any(), any())).thenReturn(new ScanDto(
                1L, "url", "rice_blast", 0.95, List.of(new PredictionDto("rice_blast", 0.95)),
                null, false, false, null, null, null, null, null));
        var image = new MockMultipartFile("image", "leaf.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/scans").file(image)
                        .header("Authorization", "Bearer " + jwtService.generateAccessToken(farmer)))
                .andExpect(status().isOk());
    }

    // BE-WEB-SCAN-04
    @Test
    void history_withoutAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/scans")).andExpect(status().isUnauthorized());
    }
}
