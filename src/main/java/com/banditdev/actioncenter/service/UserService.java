package com.banditdev.actioncenter.service;

import com.banditdev.actioncenter.model.user.User;
import com.banditdev.actioncenter.model.user.dto.UserRequest;
import com.banditdev.actioncenter.model.user.dto.UserResponse;
import com.banditdev.actioncenter.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists!");
        }
        User user = new User(request.username(), request.password(), request.role());
        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
        return UserResponse.from(user);
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
