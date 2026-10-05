package com.banditdev.actioncenter.model.user.dto;

import com.banditdev.actioncenter.model.user.Role;

public record UserRequest(String username, String password, Role role) {
}