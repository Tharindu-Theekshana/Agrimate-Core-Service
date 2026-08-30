package com.agrimate.service.service;

import com.agrimate.service.dto.NewsDtos.NewsDto;
import com.agrimate.service.dto.NewsDtos.NewsRequest;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.news.News;
import com.agrimate.service.model.notification.Notification;
import com.agrimate.service.model.notification.NotificationType;
import com.agrimate.service.repository.AccountRepository;
import com.agrimate.service.repository.NewsRepository;
import com.agrimate.service.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock private NewsRepository newsRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private PushService pushService;
    private NewsService newsService;

    @BeforeEach
    void setUp() {
        newsService = new NewsService(newsRepository, accountRepository, notificationRepository, pushService);
    }

    // BE-NEWS-01
    @Test
    void list_returnsAllNewsNewestFirst() {
        News n = new News();
        n.setTitle("Monsoon advisory");
        when(newsRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(n));

        List<NewsDto> news = newsService.list();

        assertThat(news).hasSize(1);
        assertThat(news.get(0).title()).isEqualTo("Monsoon advisory");
    }

    // BE-NEWS-02
    @Test
    void create_savesTheArticleAndBroadcastsANewsNotificationToEveryAccount() {
        when(newsRepository.save(any(News.class))).thenAnswer(inv -> {
            News n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
        Account a1 = accountWithTokens(Map.of("tok-1", "android"));
        Account a2 = accountWithTokens(Map.of("tok-2", "ios"));
        when(accountRepository.findAll()).thenReturn(List.of(a1, a2));

        NewsDto dto = newsService.create(new NewsRequest("New Advisory", "Body text", null));

        assertThat(dto.title()).isEqualTo("New Advisory");
        ArgumentCaptor<Notification> notifCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notifCaptor.capture());
        assertThat(notifCaptor.getValue().getType()).isEqualTo(NotificationType.NEWS);
        assertThat(notifCaptor.getValue().getAccount()).isNull(); // broadcast, not account-scoped
        verify(pushService).sendToTokens(List.of("tok-1", "tok-2"), "New Advisory", "Body text", "NEWS");
    }

    // BE-NEWS-03
    @Test
    void delete_removesAnExistingArticle() {
        when(newsRepository.existsById(1L)).thenReturn(true);

        newsService.delete(1L);

        verify(newsRepository).deleteById(1L);
    }

    // BE-NEWS-04
    @Test
    void delete_throwsNotFound_whenTheArticleDoesNotExist() {
        when(newsRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> newsService.delete(99L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-NEWS-05
    @Test
    void create_trimsTitleAndDescriptionBeforeSaving() {
        when(newsRepository.save(any(News.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.findAll()).thenReturn(List.of());

        NewsDto dto = newsService.create(new NewsRequest("  Padded Title  ", "  Padded body  ", null));

        assertThat(dto.title()).isEqualTo("Padded Title");
        assertThat(dto.description()).isEqualTo("Padded body");
    }

    private Account accountWithTokens(Map<String, String> tokens) {
        Account a = new Account();
        a.setDeviceTokens(new HashMap<>(tokens));
        return a;
    }
}
