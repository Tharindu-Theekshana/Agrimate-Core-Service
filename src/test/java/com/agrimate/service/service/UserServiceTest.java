package com.agrimate.service.service;

import com.agrimate.service.dto.RegisterDeviceTokenRequest;
import com.agrimate.service.dto.UpdateUserRequest;
import com.agrimate.service.dto.UserDto;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.AccountRepository;
import com.agrimate.service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private StorageService storageService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, accountRepository, storageService);
    }

    private User userWithAccount(long id, RoleName accountType) {
        User user = new User();
        user.setId(id);
        Account account = new Account();
        account.setId(id);
        account.setName("Farmer " + id);
        account.setAccountType(accountType);
        account.setUser(user);
        user.setAccount(account);
        return user;
    }

    // BE-USR-01
    @Test
    void me_throwsNotFound_whenTheUserDoesNotExist() {
        when(userRepository.findDetailById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.me(1L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-USR-02
    @Test
    void updateMe_appliesNonBlankNameAndLocationToTheAccount() {
        Account account = userWithAccount(1L, RoleName.FARMER).getAccount();
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));
        when(userRepository.findDetailById(1L)).thenReturn(Optional.of(account.getUser()));

        userService.updateMe(1L, new UpdateUserRequest("New Name", "Colombo", null));

        assertThat(account.getName()).isEqualTo("New Name");
        assertThat(account.getLocation()).isEqualTo("Colombo");
    }

    // BE-USR-03
    @Test
    void updateMe_ignoresABlankName() {
        Account account = userWithAccount(1L, RoleName.FARMER).getAccount();
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));
        when(userRepository.findDetailById(1L)).thenReturn(Optional.of(account.getUser()));

        userService.updateMe(1L, new UpdateUserRequest("   ", null, null));

        assertThat(account.getName()).isEqualTo("Farmer 1");
    }

    // BE-USR-04
    @Test
    void updateMe_throwsNotFound_whenTheAccountDoesNotExist() {
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateMe(1L, new UpdateUserRequest("x", null, null)))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-USR-05
    @Test
    void uploadPhoto_throwsBadRequest_whenNoImageIsProvided() {
        assertThatThrownBy(() -> userService.uploadPhoto(1L, null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // BE-USR-06
    @Test
    void uploadPhoto_success_setsTheAccountsProfilePhotoUrl() {
        Account account = userWithAccount(1L, RoleName.FARMER).getAccount();
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));
        when(storageService.upload(any())).thenReturn("https://cdn/avatar.jpg");
        when(userRepository.findDetailById(1L)).thenReturn(Optional.of(account.getUser()));
        var image = new MockMultipartFile("image", "a.jpg", "image/jpeg", new byte[]{1});

        UserDto dto = userService.uploadPhoto(1L, image);

        assertThat(account.getProfilePhotoUrl()).isEqualTo("https://cdn/avatar.jpg");
    }

    // BE-USR-07
    @Test
    void uploadAgronomistProof_throwsForbidden_forANonAgronomistAccount() {
        Account account = userWithAccount(1L, RoleName.FARMER).getAccount();
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));
        var image = new MockMultipartFile("image", "proof.jpg", "image/jpeg", new byte[]{1});

        assertThatThrownBy(() -> userService.uploadAgronomistProof(1L, image))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
        verify(storageService, never()).upload(any(), any());
    }

    // BE-USR-08
    @Test
    void uploadAgronomistProof_success_forAnAgronomistAccount() {
        Account account = userWithAccount(1L, RoleName.AGRONOMIST).getAccount();
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));
        when(storageService.upload(any(), any())).thenReturn("https://cdn/proof.jpg");
        when(userRepository.findDetailById(1L)).thenReturn(Optional.of(account.getUser()));
        var image = new MockMultipartFile("image", "proof.jpg", "image/jpeg", new byte[]{1});

        userService.uploadAgronomistProof(1L, image);

        assertThat(account.getAgronomistProofUrl()).isEqualTo("https://cdn/proof.jpg");
    }

    // BE-USR-09
    @Test
    void registerDeviceToken_movesATokenAlreadyBoundToAnotherAccount() {
        Account mine = userWithAccount(1L, RoleName.FARMER).getAccount();
        Account other = userWithAccount(2L, RoleName.FARMER).getAccount();
        other.setDeviceTokens(new HashMap<>(java.util.Map.of("shared-token", "android")));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(mine));
        when(accountRepository.findByDeviceToken("shared-token")).thenReturn(Optional.of(other));

        userService.registerDeviceToken(1L, new RegisterDeviceTokenRequest("shared-token", "android"));

        assertThat(other.getDeviceTokens()).doesNotContainKey("shared-token");
        assertThat(mine.getDeviceTokens()).containsEntry("shared-token", "android");
    }

    // BE-USR-10
    @Test
    void registerDeviceToken_defaultsPlatformToUnknown_whenNotProvided() {
        Account mine = userWithAccount(1L, RoleName.FARMER).getAccount();
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(mine));
        when(accountRepository.findByDeviceToken("tok")).thenReturn(Optional.empty());

        userService.registerDeviceToken(1L, new RegisterDeviceTokenRequest("tok", null));

        assertThat(mine.getDeviceTokens()).containsEntry("tok", "unknown");
    }
}
