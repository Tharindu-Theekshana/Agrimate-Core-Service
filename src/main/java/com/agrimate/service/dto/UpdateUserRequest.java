package com.agrimate.service.dto;

public record UpdateUserRequest(
        String name,
        String location,
        String profilePhotoUrl
) {}
