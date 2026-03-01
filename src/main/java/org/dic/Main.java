package org.dic;

import org.dic.components.BeanDefinitionFactory;
import org.dic.components.DIC;
import org.dic.dto.BeanDefinition;
import org.dic.repository.UserRepository;
import org.dic.services.AuthService;
import org.dic.services.JWTService;
import org.dic.services.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        //The main goal is IoC, giving the control of beans lifecycle to the DIC
        //First we need to scan the beans, construct Dependency Graph then resolve
        //We'd need BeanDefinition Class, Could be built dynamically by scanning packages
        // or Hardcoded

        BeanDefinition<UserService> userServiceBeanDefinition = BeanDefinitionFactory
                .builder(UserService.class)
                .beanName("UserService")
                .build();

        BeanDefinition<AuthService> authServiceBeanDefinition = BeanDefinitionFactory
                .builder(AuthService.class)
                .beanName("AuthService")
                .build();

        BeanDefinition<JWTService> jwtServiceBeanDefinition = BeanDefinitionFactory
                .builder(JWTService.class)
                .beanName("JWTService")
                .build();

        BeanDefinition<UserRepository> userRepositoryBeanDefinition = BeanDefinitionFactory
                .builder(UserRepository.class)
                .beanName("UserRepository")
                .build();

        userServiceBeanDefinition.setDependencies(new ArrayList<>(
                Arrays.asList(
                        authServiceBeanDefinition,
                        userRepositoryBeanDefinition
                )
        ));

        authServiceBeanDefinition.setDependencies(new ArrayList<>(
                List.of(
                       jwtServiceBeanDefinition
                )
        ));

        jwtServiceBeanDefinition.setDependencies(new ArrayList<>(
                List.of(
                        userRepositoryBeanDefinition
                )
        ));

        DIC dic = new DIC(Arrays.asList(
                userRepositoryBeanDefinition,
                userServiceBeanDefinition,
                jwtServiceBeanDefinition,
                authServiceBeanDefinition
        ));

        UserService userService = dic.getInstance(UserService.class);
        userService.healthCheck();
        UserRepository userRepository = dic.getInstance(UserRepository.class);
        userRepository.healthCheck();
    }
}