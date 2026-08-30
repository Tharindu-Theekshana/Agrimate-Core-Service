package com.agrimate.service.service;

import com.agrimate.service.dto.AdminDtos.CreateAdminRequest;
import com.agrimate.service.dto.UserDto;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.role.Role;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.AccountRepository;
import com.agrimate.service.repository.NotificationRepository;
import com.agrimate.service.repository.RoleRepository;
import com.agrimate.service.repository.ScanRepository;
import com.agrimate.service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private ScanRepository scanRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PushService pushService;
    @Mock private MailService mailService;
    @Mock private PasswordEncoder passwordEncoder;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository, accountRepository, scanRepository, notificationRepository,
                roleRepository, pushService, mailService, passwordEncoder);
    }

    private CreateAdminRequest request() {
        return new CreateAdminRequest("newadmin", "newadmin@agrimate.lk", "New Admin", "0770001111", "Colombo");
    }

    private void stubHappyPath() {
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(new Role(RoleName.ADMIN, "Admin")));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(42L);
            return u;
        });
    }

    @Test
    void createAdmin_success_returnsCreatedUserDto() {
        stubHappyPath();

        UserDto dto = adminService.createAdmin(request());

        assertThat(dto.username()).isEqualTo("newadmin");
        assertThat(dto.email()).isEqualTo("newadmin@agrimate.lk");
    }

    @Test
    void createAdmin_generatesSixDigitTempPassword_hashesItAndEmailsItToTheNewAdmin() {
        stubHappyPath();

        adminService.createAdmin(request());

        ArgumentCaptor<String> rawPassword = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(rawPassword.capture());
        assertThat(rawPassword.getValue()).matches("\\d{6}");

        ArgumentCaptor<String> emailHtml = ArgumentCaptor.forClass(String.class);
        verify(mailService).send(eq("newadmin@agrimate.lk"), anyString(), emailHtml.capture());
        assertThat(emailHtml.getValue()).contains(rawPassword.getValue());
        assertThat(emailHtml.getValue()).contains("newadmin");
    }

    @Test
    void createAdmin_throwsConflict_whenUsernameAlreadyTaken() {
        when(userRepository.existsByUsername("newadmin")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createAdmin(request()))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
        verify(mailService, never()).send(any(), any(), any());
    }

    @Test
    void createAdmin_throwsConflict_whenEmailAlreadyRegistered() {
        when(userRepository.existsByEmail("newadmin@agrimate.lk")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createAdmin(request()))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createAdmin_throwsConflict_whenPhoneAlreadyRegistered() {
        when(accountRepository.existsByPhone("0770001111")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createAdmin(request()))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createAdmin_throwsServerError_whenAdminRoleNotProvisionedInDb() {
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.createAdmin(request()))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(mailService, never()).send(any(), any(), any());
    }
}
