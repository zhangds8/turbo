package ds.young.turbo.spring.annotation;


import ds.young.turbo.common.RegisterInfoProvider;
import ds.young.turbo.common.TurboSpringProviderBean;
import ds.young.turbo.common.annotation.Turbo;
import ds.young.turbo.protocol.turbo.channelpool.NettyChannelPoolFactory;
import ds.young.turbo.register.zk.RegisterCenter;
import ds.young.turbo.spring.TurboProperties;
import ds.young.turbo.spring.proxy.ProxyFactory;
import ds.young.turbo.spring.utils.BinderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @program: turbo-netty
 * @description
 * @author: zhangds
 * <p>
 * 有道无术，术尚可求，有术无道，止于术
 * @create: 2020-05-26 15:27
 **/
public class TurboReferenceAnnotationBeanPostProcessor implements BeanClassLoaderAware, EnvironmentAware, BeanFactoryPostProcessor, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(TurboReferenceAnnotationBeanPostProcessor.class);

    private ClassLoader classLoader;

    private ConfigurableEnvironment environment;

    private ApplicationContext context;

    private Map<String, BeanDefinition> beanDefinitions = new LinkedHashMap();

    private ConfigurableListableBeanFactory beanFactory;



    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        this.postProcessBeanFactory(beanFactory, (BeanDefinitionRegistry)beanFactory);
    }

    private void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry) {
        final TurboProperties properties = BinderUtils.bind(environment,"spring.turbo", TurboProperties.class);
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        int beanDefinitionSize = beanDefinitionNames.length;

        for(int i = 0; i < beanDefinitionSize; ++i) {
            String beanName = beanDefinitionNames[i];
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            String beanClassName = definition.getBeanClassName();
            if (beanClassName != null) {
                Class<?> clazz = ClassUtils.resolveClassName(definition.getBeanClassName(), this.classLoader);
                ReflectionUtils.doWithFields(clazz, TurboReferenceAnnotationBeanPostProcessor.this::parseElement);
            }
        }

        Iterator iterator = this.beanDefinitions.keySet().iterator();

        while(iterator.hasNext()) {
            String beanName = (String)iterator.next();
            if (this.context.containsBean(beanName)) {
                throw new IllegalArgumentException("[HSF Starter] Spring context already has a bean named " + beanName + ", please change @HSFConsumer field name.");
            }

            registry.registerBeanDefinition(beanName, this.beanDefinitions.get(beanName));
            logger.info("在Spring上下文注册 TurboBean \"{}\" .", beanName);
        }

        //获取服务注册中心
        RegisterCenter registerCenter4Consumer = RegisterCenter.getInstance(properties.getZkServer());
        //初始化服务提供者列表到本地缓存
        registerCenter4Consumer.initProviderMap();
        //初始化Netty Channel
        Map<String, List<RegisterInfoProvider>> providerMap = registerCenter4Consumer.getServiceMetaDataMap4Consumer();
        if (providerMap == null || providerMap.size() == 0) {
            throw new RuntimeException("服务端注册服务为空！");
        }
        NettyChannelPoolFactory.getInstance().initNettyChannelPoolFactory(providerMap);

    }

    private void parseElement(Field field) {
        Turbo annotation = AnnotationUtils.getAnnotation(field, Turbo.class);

        if (annotation != null) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TurboSpringProviderBean.class);

            GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();

            definition.setBeanClass(ProxyFactory.class);
            definition.getConstructorArgumentValues().addIndexedArgumentValue(0, field.getType());
            definition.getConstructorArgumentValues().addIndexedArgumentValue(1, environment);

            definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);

            this.beanDefinitions.put(field.getName(), definition);
        }
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment)environment;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
