package com.agrimate.service.controller;

import com.agrimate.service.dto.NewsDtos.NewsDto;
import com.agrimate.service.repository.UserRepository;
import com.agrimate.service.security.JwtAuthenticationFilter;
import com.agrimate.service.security.JwtService;
import com.agrimate.service.service.NewsService;
import com.agrimate.service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = {NewsController.class, NotificationController.class})
@org.springframework.context.annotation.Import({
        com.agrimate.service.config.SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
class NewsNotificationControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private NewsService newsService;
    @MockitoBean private NotificationService notificationService;
    @MockitoBean private UserRepository userRepository;

    // BE-WEB-NEWS-01
    @Test
    void listNews_withNoAuthorizationHeader_returns200() throws Exception {
        when(newsService.list()).thenReturn(List.of(new NewsDto(1L, "Headline", "Body", null, null)));

        mockMvc.perform(get("/api/news")).andExpect(status().isOk());
    }

    // BE-WEB-NEWS-02
    @Test
    void listNotifications_withoutAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());
    }

    // BE-WEB-NEWS-03
    @Test
    void markAllNotificationsRead_withoutAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(post("/api/notifications/read-all")).andExpect(status().isUnauthorized());
    }
}
