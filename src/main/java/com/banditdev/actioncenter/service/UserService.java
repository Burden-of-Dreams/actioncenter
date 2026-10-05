package com.banditdev.actioncenter.service;

import com.banditdev.actioncenter.model.user.User;
import com.banditdev.actioncenter.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User login(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        User user = findByUsername(username);

        if (user == null || !password.equals(user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return user;
    }
}
