package ds.young.turbo.spring.utils;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @program: turbo-netty
 * @description
 * @author: zhangds
 * <p>
 * 有道无术，术尚可求，有术无道，止于术
 * @create: 2020-05-27 14:34
 **/
public abstract class BeanNameUtils {
    private static final AnnotationBeanNameGenerator instance = new AnnotationBeanNameGenerator();

    public BeanNameUtils() {
    }

    public static String beanName(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotatedGenericBeanDefinition annotatedBeanDefinition = new AnnotatedGenericBeanDefinition(importingClassMetadata);
        return instance.generateBeanName(annotatedBeanDefinition, registry);
    }
}
