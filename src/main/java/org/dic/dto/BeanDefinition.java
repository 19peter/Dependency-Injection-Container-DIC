package org.dic.dto;

import java.util.ArrayList;
import java.util.List;

public class BeanDefinition<T> {
    //Class
    Class<T> beanClass;

    //Scope
    boolean isSingleton = true;

    //Dependencies (List of Classes)
    List<BeanDefinition<?>> dependencies;

    //name
    String name;

    public BeanDefinition() {
        dependencies = new ArrayList<>();
    }

    public BeanDefinition(String name, Class<T> beanClass) {
        this.beanClass = beanClass;
        this.name = name;
        dependencies = new ArrayList<>();
    }

    public void setBeanClass(Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    public void setDependencies(List<BeanDefinition<?>> dependenciesList) {
        this.dependencies.addAll(dependenciesList);

    }

    public void replaceDependencies(List<BeanDefinition<?>> dependenciesList) {
        this.dependencies.clear();
        this.dependencies.addAll(dependenciesList);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public List<BeanDefinition<?>> getDependencies() {
        return dependencies;
    }

    public String getName() {
        return name;
    }
}
