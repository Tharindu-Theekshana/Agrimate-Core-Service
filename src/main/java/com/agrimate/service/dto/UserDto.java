package com.agrimate.service.dto;

import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.account.AgronomistStatus;
import com.agrimate.service.model.role.RoleName;

import java.util.List;

public record UserDto(
        Long id,
        String username,
        String email,
        String name,
        String phone,
        String location,
        String profilePhotoUrl,
        RoleName role,
        List<RoleName> roles,
        RoleName accountType,
        AgronomistStatus agronomistStatus,
        boolean suspended
) {
    public static UserDto from(User u) {
        Account a = u.getAccount();
        return new UserDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                a != null ? a.getName() : null,
                a != null ? a.getPhone() : null,
                a != null ? a.getLocation() : null,
                a != null ? a.getProfilePhotoUrl() : null,
                u.primaryRole(),
                u.getRoleNames().stream().toList(),
                a != null ? a.getAccountType() : null,
                a != null ? a.getAgronomistStatus() : AgronomistStatus.NONE,
                a != null && a.isSuspended());
    }
}
