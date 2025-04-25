package org.example.shardingservice.controller;

import org.example.shardingservice.model.User;
import org.example.shardingservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    @PostMapping
    public void createUser(@RequestBody User user) {
        userRepository.save(user);
    }
}
