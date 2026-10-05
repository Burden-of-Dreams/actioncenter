package com.banditdev.actioncenter.model.user.dto;

import com.banditdev.actioncenter.model.user.Role;
import com.banditdev.actioncenter.model.user.User;

public record UserResponse(Long id, String username, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
