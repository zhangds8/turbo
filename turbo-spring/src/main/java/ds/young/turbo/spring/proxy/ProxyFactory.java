package ds.young.turbo.spring.proxy;


import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.env.ConfigurableEnvironment;

import java.lang.reflect.Proxy;

public class ProxyFactory<T> implements FactoryBean<T> {


    private Class<T> interfaceClass;


    private ConfigurableEnvironment environment;


    public ProxyFactory(Class<T> interfaceClass, ConfigurableEnvironment environment) {
        this.interfaceClass = interfaceClass;
        this.environment = environment;
    }



    @Override
    public T getObject() throws Exception {
        return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new Handler<>(interfaceClass, environment));
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }


}
