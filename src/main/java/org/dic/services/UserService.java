package org.dic.services;

import org.dic.repository.UserRepository;

public class UserService {

    AuthService authService;
    UserRepository userRepository;
    String name;

    public UserService(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.name = "User Service";
        System.out.println("Object: " + this.name + " Is Constructed");
    }

    public void healthCheck() {
        System.out.println(this.name + " is healthy");
    }
}
