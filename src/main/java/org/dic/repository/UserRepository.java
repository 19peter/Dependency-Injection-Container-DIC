package org.dic.repository;

public class UserRepository {
    String name;
    public UserRepository() {
        this.name = "User Repository";
        System.out.println("Object: " + this.name + " Is Constructed");
    }

    public void healthCheck() {
        System.out.println(this.name + " is healthy");
    }
}
