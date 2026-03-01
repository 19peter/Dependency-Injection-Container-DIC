package org.dic.services;

import org.dic.repository.UserRepository;

public class JWTService {

    UserRepository userRepository;
    String name;

    public JWTService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.name = "JWT Service";
        System.out.println("Object: " + this.name + " Is Constructed");
    }
}
