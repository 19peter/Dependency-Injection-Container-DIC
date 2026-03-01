package org.dic.components;

import org.dic.dto.BeanDefinition;

public class BeanDefinitionFactory {

    public static <T> Builder <T> builder(Class<T> beanClass) {
        return new Builder<>(beanClass);
    }

    public static class Builder<T> {
        private final BeanDefinition<T> bd = new BeanDefinition<T>();

        public Builder(Class<T> beanClass) {
            bd.setBeanClass(beanClass);
        }

        public Builder<T> beanName(String beanName) {
            bd.setName(beanName);
            return this;
        }

        public BeanDefinition<T> build() {
            if (bd.getBeanClass() == null) {
                throw new IllegalStateException("Bean class must be set");
            }
            return bd;
        }
    }


}
