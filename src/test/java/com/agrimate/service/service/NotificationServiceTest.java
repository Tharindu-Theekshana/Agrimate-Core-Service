package com.agrimate.service.service;

import com.agrimate.service.dto.NotificationDto;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.notification.Notification;
import com.agrimate.service.model.notification.NotificationType;
import com.agrimate.service.repository.AccountRepository;
import com.agrimate.service.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private AccountRepository accountRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, accountRepository);
    }

    private Notification notif(long id, Instant createdAt) {
        Notification n = new Notification();
        n.setId(id);
        n.setType(NotificationType.SYSTEM);
        n.setTitle("Title " + id);
        n.setCreatedAt(createdAt);
        return n;
    }

    // BE-NOTIF-01
    @Test
    void list_throwsNotFound_whenTheAccountDoesNotExist() {
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.list(1L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-NOTIF-02
    @Test
    void list_mergesAccountScopedAndBroadcastNotifications_newestFirst() {
        Account account = new Account();
        account.setId(5L);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));
        Instant now = Instant.now();
        when(notificationRepository.findByAccountIdOrderByCreatedAtDesc(5L))
                .thenReturn(List.of(notif(1L, now.minus(1, ChronoUnit.HOURS))));
        when(notificationRepository.findByAccountIsNullOrderByCreatedAtDesc())
                .thenReturn(List.of(notif(2L, now)));

        List<NotificationDto> result = notificationService.list(1L);

        assertThat(result).extracting(NotificationDto::id).containsExactly(2L, 1L);
    }

    // BE-NOTIF-03
    @Test
    void list_marksNotificationsCreatedBeforeReadAt_asRead() {
        Account account = new Account();
        account.setId(5L);
        Instant readAt = Instant.now();
        account.setNotificationsReadAt(readAt);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));
        when(notificationRepository.findByAccountIdOrderByCreatedAtDesc(5L))
                .thenReturn(List.of(notif(1L, readAt.minusSeconds(60)), notif(2L, readAt.plusSeconds(60))));
        when(notificationRepository.findByAccountIsNullOrderByCreatedAtDesc()).thenReturn(List.of());

        List<NotificationDto> result = notificationService.list(1L);

        NotificationDto older = result.stream().filter(n -> n.id() == 1L).findFirst().orElseThrow();
        NotificationDto newer = result.stream().filter(n -> n.id() == 2L).findFirst().orElseThrow();
        assertThat(older.read()).isTrue();
        assertThat(newer.read()).isFalse();
    }

    // BE-NOTIF-04
    @Test
    void markAllRead_stampsTheAccountsReadAtToNow() {
        Account account = new Account();
        account.setId(5L);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));

        notificationService.markAllRead(1L);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getNotificationsReadAt()).isNotNull();
    }

    // BE-NOTIF-05
    @Test
    void markAllRead_throwsNotFound_whenTheAccountDoesNotExist() {
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAllRead(1L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }
}
