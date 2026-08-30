package com.agrimate.service.controller;

import com.agrimate.service.dto.AppVersionDtos.CheckResponse;
import com.agrimate.service.dto.DiseaseDto;
import com.agrimate.service.model.appVersion.Platform;
import com.agrimate.service.model.disease.Severity;
import com.agrimate.service.repository.UserRepository;
import com.agrimate.service.security.JwtAuthenticationFilter;
import com.agrimate.service.security.JwtService;
import com.agrimate.service.service.AppVersionService;
import com.agrimate.service.service.DiseaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = {DiseaseController.class, AppVersionController.class})
@org.springframework.context.annotation.Import({
        com.agrimate.service.config.SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
class DiseaseAppVersionControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private DiseaseService diseaseService;
    @MockitoBean private AppVersionService appVersionService;
    @MockitoBean private UserRepository userRepository;

    // BE-WEB-PUB-01
    @Test
    void listDiseases_withNoAuthorizationHeader_returns200() throws Exception {
        when(diseaseService.list()).thenReturn(List.of(new DiseaseDto(
                "rice_blast", "Rice Blast", null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, Severity.HIGH)));

        mockMvc.perform(get("/api/diseases")).andExpect(status().isOk());
    }

    // BE-WEB-PUB-02
    @Test
    void getDiseaseByKey_withNoAuthorizationHeader_returns200() throws Exception {
        when(diseaseService.get("rice_blast")).thenReturn(new DiseaseDto(
                "rice_blast", "Rice Blast", null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, Severity.HIGH));

        mockMvc.perform(get("/api/diseases/rice_blast")).andExpect(status().isOk());
    }

    // BE-WEB-PUB-03
    @Test
    void checkAppVersion_withNoAuthorizationHeader_returns200() throws Exception {
        when(appVersionService.check(any(), any())).thenReturn(new CheckResponse(false, false, "1.0.0", "1.0.0"));

        mockMvc.perform(get("/api/app-version/check").param("platform", "ANDROID").param("version", "1.0.0"))
                .andExpect(status().isOk());
    }
}
