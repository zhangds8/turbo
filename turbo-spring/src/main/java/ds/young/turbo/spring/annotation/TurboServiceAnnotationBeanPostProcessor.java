package ds.young.turbo.spring.annotation;

import ds.young.turbo.common.RegisterInfoProvider;
import ds.young.turbo.common.TurboSpringProviderBean;
import ds.young.turbo.common.URL;
import ds.young.turbo.common.annotation.TurboService;
import ds.young.turbo.protocol.Procotol;
import ds.young.turbo.protocol.turbo.TurboProcotol;
import ds.young.turbo.register.RegisterCenter4Provider;
import ds.young.turbo.register.zk.RegisterCenter;
import ds.young.turbo.spring.RoleType;
import ds.young.turbo.spring.TurboProperties;

import ds.young.turbo.spring.utils.BinderUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import static java.util.Arrays.asList;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * @program: turbo-netty
 * @description
 * @author: zhangds
 * <p>
 * 有道无术，术尚可求，有术无道，止于术
 * @create: 2020-05-26 15:27
 **/
public class TurboServiceAnnotationBeanPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, ResourceLoaderAware, BeanClassLoaderAware{

    private static final Logger logger = LoggerFactory.getLogger(TurboServiceAnnotationBeanPostProcessor.class);

    private ResourceLoader resourceLoader;

    private ClassLoader classLoader;

    private ConfigurableEnvironment environment;

    protected final Set<String> packagesToScan;

    private TurboProperties turboProperties;

    private final static List<Class<? extends Annotation>> serviceAnnotationTypes = asList(TurboService.class);

    public TurboServiceAnnotationBeanPostProcessor(Set<String> packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {

        BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

        TurboScanner turboScanner = new TurboScanner(beanDefinitionRegistry, this.environment, this.resourceLoader);

        serviceAnnotationTypes.forEach(annotationType -> {
            turboScanner.addIncludeFilter(new AnnotationTypeFilter(annotationType));
        });

        for (String aPackage : packagesToScan) {
            turboScanner.scan(aPackage);
            Set<BeanDefinition> beanDefinitions = turboScanner.findCandidateComponents(aPackage);

            Set<BeanDefinitionHolder> beanDefinitionHolders = getBeanDefinitionHolders(beanDefinitionRegistry, beanNameGenerator, beanDefinitions);

            for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
                registerTurboService(beanDefinitionHolder, beanDefinitionRegistry);
            }
            logger.info("[ TURBO ] {} 个注解 Turbo's @TurboService 被扫描注册，包名：{}", beanDefinitionHolders.size(), aPackage);
        }

        if(RoleType.PROVIDER.getCode().equals(turboProperties.getRoleType())){
            // 启动netty
            String protocol = turboProperties.getProtocol();
            Integer nettyPort = turboProperties.getNettyPort();

            if(StringUtils.isEmpty(protocol)){
                protocol = "turbo";
            }
            // 暂时实现turbo协议
            Procotol procotol = protocol.equalsIgnoreCase("turbo") ? new TurboProcotol() : new TurboProcotol();

            try {
                InetAddress addr = InetAddress.getLocalHost();
                String ip = addr.getHostAddress();

                if(nettyPort == 0 || null == nettyPort){
                    nettyPort = 12185;
                }
                URL url = new URL(ip, nettyPort);
                procotol.start(url);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

    }

    private void registerTurboService(BeanDefinitionHolder beanDefinitionHolder, BeanDefinitionRegistry registry){

        // 接口实现类
        Class<?> className = resolveClass(beanDefinitionHolder);
        // 实现类名
        String beanName = beanDefinitionHolder.getBeanName();
        try {
            // 实现类注解
            Annotation serviceAnnotation = findServiceAnnotation(className);
            // 注解属性
            AnnotationAttributes serviceAnnotationAttributes = getAnnotationAttributes(serviceAnnotation, false, false);
            // 注解接口名
            Class interfaceClass = (Class<?>)serviceAnnotationAttributes.get("interfaceClass");

            Assert.notNull(interfaceClass, "interfaceClass()为必填项!");

            String annotatedServiceBeanName = beanDefinitionHolder.getBeanName();

            // 构建bean定义
            AbstractBeanDefinition serviceBeanDefinition = buildServiceBeanDefinition(serviceAnnotationAttributes, turboProperties, interfaceClass, annotatedServiceBeanName);

            String beanFinalName = "serviceBean" + ":" + className.getName();

            // 判断是否为空
            if (serviceBeanDefinition != null) {
                // 判断有无重名的Bean
                if (registry.containsBeanDefinition(beanFinalName)) {
                    throw new BeanDefinitionValidationException("同名bean已存在, 请检查配置! beanName:" + beanFinalName);
                }
                // 注入Spring
                registry.registerBeanDefinition(beanFinalName, serviceBeanDefinition);
                logger.info("[ TURBO ] 已注册bean名称: {}, 实现类: {}", beanFinalName, className);
            }
            // zk注册
            List providerServices = new ArrayList<RegisterInfoProvider>();
            RegisterInfoProvider providerService = new RegisterInfoProvider();
            providerService.setProvider(Class.forName(interfaceClass.getName()));
            providerService.setServiceObject(className.getName());
            providerService.setIp("127.0.0.1");
            providerService.setPort(turboProperties.getNettyPort());
            providerService.setTimeout(5000);
            providerService.setServiceMethod(null);
            providerService.setApplicationName("aaa");
            providerService.setGroupName("nettyrpc");
            providerServices.add(providerService);
            //注册到zk,元数据注册中心
            RegisterCenter4Provider registerCenter4Provider = RegisterCenter.getInstance(turboProperties.getZkServer());
            registerCenter4Provider.registerProvider(providerServices);
        } catch (Exception e) {
            throw new BeanCreationException("发布服务异常，beanName：" + beanName, e);
        }
    }

    /**
     * 构建bean定义
     * @param annotationAttributes
     * @param turboProperties
     * @param interfaceClass
     * @param annotatedServiceBeanName
     * @return
     */
    private AbstractBeanDefinition buildServiceBeanDefinition(AnnotationAttributes annotationAttributes, TurboProperties turboProperties, Class<?> interfaceClass, String annotatedServiceBeanName){

        BeanDefinitionBuilder builder = rootBeanDefinition(TurboSpringProviderBean.class);

        // References "ref" property to annotated-@Service Bean
        addPropertyReference(builder, "target", annotatedServiceBeanName);
        // Set interface
        builder.addPropertyValue("serviceInterface", interfaceClass.getName());
        builder.addPropertyValue("group", turboProperties.getGroup());
        builder.addPropertyValue("serviceDesc", annotationAttributes.getString("serviceDesc"));
        builder.addPropertyValue("serviceName", annotationAttributes.getString("serviceName"));
        builder.addPropertyValue("timeout", annotationAttributes.getString("timeout"));
        builder.addPropertyValue("serializeType", turboProperties.getSerializeType());
        builder.addPropertyValue("version", turboProperties.getVersion());

        return builder.getBeanDefinition();
    }

    private Class<?> resolveClass(BeanDefinitionHolder beanDefinitionHolder) {

        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();

        return resolveClass(beanDefinition);

    }
    private Class<?> resolveClass(BeanDefinition beanDefinition) {

        String beanClassName = beanDefinition.getBeanClassName();

        return resolveClassName(beanClassName, classLoader);
    }

    private Set<BeanDefinitionHolder> getBeanDefinitionHolders(BeanDefinitionRegistry beanDefinitionRegistry, BeanNameGenerator beanNameGenerator, Set<BeanDefinition> beanDefinitions) {
        Set<BeanDefinitionHolder> beanDefinitionHolders = new LinkedHashSet(beanDefinitions.size());
        for (BeanDefinition beanDefinition : beanDefinitions) {
            String beanName = beanNameGenerator.generateBeanName(beanDefinition, beanDefinitionRegistry);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
            beanDefinitionHolders.add(beanDefinitionHolder);
        }
        return beanDefinitionHolders;
    }

    private Annotation findServiceAnnotation(Class<?> beanClass) {
        return serviceAnnotationTypes
                .stream()
                .map(annotationType -> findMergedAnnotation(beanClass, annotationType))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private void addPropertyReference(BeanDefinitionBuilder builder, String propertyName, String beanName) {
        String resolvedBeanName = environment.resolvePlaceholders(beanName);
        builder.addPropertyReference(propertyName, resolvedBeanName);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment)environment;
        this.turboProperties = BinderUtils.bind(this.environment, "spring.turbo", TurboProperties.class);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
