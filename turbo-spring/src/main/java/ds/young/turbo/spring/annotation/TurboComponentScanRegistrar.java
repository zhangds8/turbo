package ds.young.turbo.spring.annotation;

import ds.young.turbo.spring.TurboProperties;
import ds.young.turbo.spring.utils.BinderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * @program: turbo-netty
 * @description
 * @author: zhangds
 * <p>
 * 有道无术，术尚可求，有术无道，止于术
 * @create: 2020-05-25 16:36
 **/
@Configuration
@ConditionalOnProperty(name = {"spring.turbo.enabled"}, matchIfMissing = true)
@EnableConfigurationProperties({TurboProperties.class})
public class TurboComponentScanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private ConfigurableEnvironment environment;

    private static final Logger logger = LoggerFactory.getLogger(TurboComponentScanRegistrar.class);


    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {

        // 构建hsfBean属性 取application.properties配置
        // 构建服务属性
        TurboProperties turboProperties = BinderUtils.bind(this.environment, "spring.turbo", TurboProperties.class);

        if (!turboProperties.isEnabled()) {
            if (logger.isInfoEnabled()) {
                logger.info("配置不启用spring.turbo.enabled 不生效, 所以取消服务导出，@TurboService注解不生效。");
            }
        }else {
            //根据传入的importingClassMetadata找到贴有DubboComponentScan标签类的信息，从中获取需要扫描的包
            Set<String> packagesToScan = getPackagesToScan(annotationMetadata);
            //注册ServiceAnnotationBeanPostProcessor，这里会把packagesToScan当作一个构造器的构造参数加入
            registerServiceAnnotationBeanPostProcessor(packagesToScan, beanDefinitionRegistry);
            // 客户端注入
//            registerReferenceAnnotationBeanPostProcessor(packagesToScan, beanDefinitionRegistry);
        }
    }

    private void registerServiceAnnotationBeanPostProcessor(Set<String> packagesToScan, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = rootBeanDefinition(TurboServiceAnnotationBeanPostProcessor.class);
        builder.addConstructorArgValue(packagesToScan);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
    }

//    private void registerReferenceAnnotationBeanPostProcessor(Set<String> packagesToScan, BeanDefinitionRegistry registry) {
//        BeanDefinitionBuilder builder = rootBeanDefinition(TurboReferenceAnnotationBeanPostProcessor.class);
//        builder.addConstructorArgValue(packagesToScan);
//        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
//        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
//        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
//    }

    /**
     * 获取注解扫描包
     * @param metadata
     * @return
     */
    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableTurbo.class.getName()));
        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        String[] value = attributes.getStringArray("value");
        Set<String> packagesToScan = new LinkedHashSet<String>(Arrays.asList(value));
        packagesToScan.addAll(Arrays.asList(basePackages));
        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
        }
        if (packagesToScan.isEmpty()) {
            return Collections.singleton(ClassUtils.getPackageName(metadata.getClassName()));
        }
        return packagesToScan;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment)environment;
    }
}
