package com.agrimate.service.controller;

import com.agrimate.service.dto.RegisterDeviceTokenRequest;
import com.agrimate.service.dto.UpdateUserRequest;
import com.agrimate.service.dto.UserDto;
import com.agrimate.service.model.user.User;
import com.agrimate.service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal User user) {
        return userService.me(user.getId());
    }

    @PatchMapping("/me")
    public UserDto updateMe(@AuthenticationPrincipal User user, @RequestBody UpdateUserRequest req) {
        return userService.updateMe(user.getId(), req);
    }

    @PostMapping(value = "/me/photo", consumes = "multipart/form-data")
    public UserDto uploadPhoto(@AuthenticationPrincipal User user, @RequestParam("image") MultipartFile image) {
        return userService.uploadPhoto(user.getId(), image);
    }

    @PostMapping("/me/device-token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerDeviceToken(@AuthenticationPrincipal User user, @Valid @RequestBody RegisterDeviceTokenRequest req) {
        userService.registerDeviceToken(user.getId(), req);
    }
}
