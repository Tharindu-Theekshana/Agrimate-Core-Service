package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.UpdateUserRequest;
import com.agrimate.service.dto.UserDto;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.AccountRepository;
import com.agrimate.service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final StorageService storageService;

    public UserService(UserRepository userRepository, AccountRepository accountRepository,
                       StorageService storageService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        return UserDto.from(load(userId));
    }

    @Transactional
    public UserDto updateMe(Long userId, UpdateUserRequest req) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Account not found"));
        if (req.name() != null && !req.name().isBlank()) account.setName(req.name().trim());
        if (req.location() != null) account.setLocation(req.location());
        if (req.profilePhotoUrl() != null) account.setProfilePhotoUrl(req.profilePhotoUrl());
        accountRepository.save(account);
        return UserDto.from(load(userId));
    }

    @Transactional
    public UserDto uploadPhoto(Long userId, MultipartFile image) {
        if (image == null || image.isEmpty()) throw ApiException.badRequest("An image is required");
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Account not found"));
        account.setProfilePhotoUrl(storageService.upload(image));
        accountRepository.save(account);
        return UserDto.from(load(userId));
    }

    private User load(Long userId) {
        return userRepository.findDetailById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
