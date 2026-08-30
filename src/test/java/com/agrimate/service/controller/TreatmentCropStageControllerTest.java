package com.agrimate.service.controller;

import com.agrimate.service.repository.UserRepository;
import com.agrimate.service.security.JwtAuthenticationFilter;
import com.agrimate.service.security.JwtService;
import com.agrimate.service.service.CropStageLogService;
import com.agrimate.service.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TreatmentController.class, CropStageLogController.class})
@org.springframework.context.annotation.Import({
        com.agrimate.service.config.SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
class TreatmentCropStageControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TreatmentService treatmentService;
    @MockitoBean private CropStageLogService cropStageLogService;
    @MockitoBean private UserRepository userRepository;

    // BE-WEB-CROP-01
    @Test
    void listTreatments_withoutAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/crops/1/treatments")).andExpect(status().isUnauthorized());
    }

    // BE-WEB-CROP-02
    @Test
    void listCropStages_withoutAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/crops/1/stages")).andExpect(status().isUnauthorized());
    }
}
