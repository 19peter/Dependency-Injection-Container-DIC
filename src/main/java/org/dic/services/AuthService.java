package org.dic.services;

public class AuthService {

    JWTService jwtService;
    String name;

    public AuthService(JWTService jwtService) {
        this.jwtService = jwtService;
        this.name = "Auth Service";
        System.out.println("Object: " + this.name + " Is Constructed");
    }
}
