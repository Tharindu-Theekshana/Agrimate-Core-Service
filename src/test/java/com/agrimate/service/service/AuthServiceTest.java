package com.agrimate.service.service;

import com.agrimate.service.dto.AuthDtos.AuthResponse;
import com.agrimate.service.dto.AuthDtos.LoginRequest;
import com.agrimate.service.dto.AuthDtos.RegisterRequest;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.role.Role;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.AccountRepository;
import com.agrimate.service.repository.RoleRepository;
import com.agrimate.service.repository.UserRepository;
import com.agrimate.service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private StorageService storageService;
    @Mock private OtpService otpService;
    @Mock private MailService mailService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, accountRepository, roleRepository, passwordEncoder,
                jwtService, storageService, otpService, mailService, eventPublisher);
    }

    private RegisterRequest farmerRequest() {
        return new RegisterRequest("kasun", "kasun@agrimate.lk", "secret123", "Kasun Perera",
                "0771234567", "Kurunegala", RoleName.FARMER);
    }

    private void stubHappyPathSave() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(roleRepository.findByName(RoleName.FARMER)).thenReturn(Optional.of(new Role(RoleName.FARMER, "Farmer")));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
    }

    @Test
    void register_success_savesFarmerAndReturnsTokens() {
        stubHappyPathSave();

        AuthResponse res = authService.register(farmerRequest(), "654321", null);

        assertThat(res.accessToken()).isEqualTo("access-token");
        assertThat(res.refreshToken()).isEqualTo("refresh-token");
        verify(otpService).verify(eq("kasun@agrimate.lk"), any(), eq("654321"));
        verify(eventPublisher, times(1))
                .publishEvent(any(com.agrimate.service.event.UserRegisteredEvent.class));
    }

    @Test
    void register_throwsConflict_whenUsernameAlreadyTaken() {
        when(userRepository.existsByUsername("kasun")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(farmerRequest(), "654321", null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
        verify(otpService, never()).verify(any(), any(), any());
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyRegistered() {
        when(userRepository.existsByEmail("kasun@agrimate.lk")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(farmerRequest(), "654321", null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void register_throwsConflict_whenPhoneAlreadyRegistered() {
        when(accountRepository.existsByPhone("0771234567")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(farmerRequest(), "654321", null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void register_throwsBadRequest_whenOtpCodeIsInvalid() {
        doThrow(ApiException.badRequest("Invalid or expired code"))
                .when(otpService).verify(any(), any(), any());

        assertThatThrownBy(() -> authService.register(farmerRequest(), "000000", null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsBadRequest_whenAgronomistHasNoProofImage() {
        RegisterRequest req = new RegisterRequest("agro1", "agro1@agrimate.lk", "secret123", "Agro One",
                null, null, RoleName.AGRONOMIST);

        assertThatThrownBy(() -> authService.register(req, "654321", null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        verify(storageService, never()).upload(any(), anyString());
    }

    @Test
    void register_agronomistWithProof_uploadsProofAndSavesPendingStatus() {
        RegisterRequest req = new RegisterRequest("agro1", "agro1@agrimate.lk", "secret123", "Agro One",
                null, null, RoleName.AGRONOMIST);
        MultipartFile proof = new MockMultipartFile("proofImage", "proof.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(roleRepository.findByName(RoleName.AGRONOMIST)).thenReturn(Optional.of(new Role(RoleName.AGRONOMIST, "Agronomist")));
        when(storageService.upload(eq(proof), anyString())).thenReturn("https://cdn/agrimate/proof.jpg");
        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(savedUser.capture())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(jwtService.generateAccessToken(any())).thenReturn("t");
        when(jwtService.generateRefreshToken(any())).thenReturn("r");

        authService.register(req, "654321", proof);

        Account account = savedUser.getValue().getAccount();
        assertThat(account.getAgronomistProofUrl()).isEqualTo("https://cdn/agrimate/proof.jpg");
        assertThat(account.getAgronomistStatus().name()).isEqualTo("PENDING");
    }

    @Test
    void login_success_returnsTokensForMatchingCredentials() {
        User user = userWithPassword("hashed");
        when(userRepository.findDetailByUsernameOrEmail("kasun")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        AuthResponse res = authService.login(new LoginRequest("kasun", "secret123"));

        assertThat(res.accessToken()).isEqualTo("access-token");
    }

    @Test
    void login_throwsBadRequest_whenIdentifierNotFound() {
        when(userRepository.findDetailByUsernameOrEmail("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost", "whatever")))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_throwsBadRequest_whenPasswordDoesNotMatch() {
        User user = userWithPassword("hashed");
        when(userRepository.findDetailByUsernameOrEmail("kasun")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("kasun", "wrong")))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_throwsForbidden_whenAccountIsSuspended() {
        User user = userWithPassword("hashed");
        user.getAccount().setSuspended(true);
        when(userRepository.findDetailByUsernameOrEmail("kasun")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("kasun", "secret123")))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void requestPasswordReset_doesNothingAndDoesNotLeakWhetherEmailExists() {
        when(userRepository.findDetailByUsernameOrEmail("ghost@agrimate.lk")).thenReturn(Optional.empty());

        authService.requestPasswordReset("ghost@agrimate.lk");

        verify(otpService, never()).issue(any(), any());
        verify(mailService, never()).send(any(), any(), any());
    }

    @Test
    void requestPasswordReset_issuesOtpAndEmailsCode_whenAccountExists() {
        User user = userWithPassword("hashed");
        when(userRepository.findDetailByUsernameOrEmail("kasun@agrimate.lk")).thenReturn(Optional.of(user));
        when(otpService.issue(any(), any())).thenReturn("111222");
        when(otpService.ttlMinutes()).thenReturn(10);

        authService.requestPasswordReset("kasun@agrimate.lk");

        verify(mailService).send(eq("kasun@agrimate.lk"), anyString(), anyString());
    }

    @Test
    void confirmPasswordReset_success_reEncodesPasswordAndReturnsFreshTokens() {
        User user = userWithPassword("old-hash");
        when(userRepository.findDetailByUsernameOrEmail("kasun@agrimate.lk")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newSecret1")).thenReturn("new-hash");
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        AuthResponse res = authService.confirmPasswordReset("kasun@agrimate.lk", "111222", "newSecret1");

        verify(otpService).verify("kasun@agrimate.lk", com.agrimate.service.model.otp.OtpPurpose.PASSWORD_RESET, "111222");
        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("new-hash");
        assertThat(res.accessToken()).isEqualTo("access-token");
    }

    @Test
    void confirmPasswordReset_throwsBadRequest_whenAccountNotFound() {
        when(userRepository.findDetailByUsernameOrEmail("ghost@agrimate.lk")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.confirmPasswordReset("ghost@agrimate.lk", "111222", "newSecret1"))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private User userWithPassword(String hash) {
        User user = new User();
        user.setId(9L);
        user.setUsername("kasun");
        user.setEmail("kasun@agrimate.lk");
        user.setPassword(hash);
        Account account = new Account();
        account.setUser(user);
        account.setName("Kasun Perera");
        user.setAccount(account);
        return user;
    }
}
