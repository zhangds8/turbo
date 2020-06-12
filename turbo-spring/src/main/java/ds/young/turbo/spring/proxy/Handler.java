package ds.young.turbo.spring.proxy;

import ds.young.turbo.common.RegisterInfoProvider;
import ds.young.turbo.common.TurboRequest;
import ds.young.turbo.common.URL;
import ds.young.turbo.protocol.Procotol;
import ds.young.turbo.protocol.loadbalance.LoadBalanceEngine;
import ds.young.turbo.protocol.loadbalance.LoadBalanceEnum;
import ds.young.turbo.protocol.loadbalance.LoadStrategy;
import ds.young.turbo.protocol.turbo.TurboProcotol;
import ds.young.turbo.register.RegisterCenter4Consumer;
import ds.young.turbo.register.zk.RegisterCenter;
import ds.young.turbo.spring.TurboProperties;
import ds.young.turbo.spring.utils.BinderUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class Handler<T> implements InvocationHandler, EnvironmentAware {

    private Class<T> interfaceClass;

    private ConfigurableEnvironment environment;

    public Handler(Class<T> interfaceClass, ConfigurableEnvironment environment) {
        this.interfaceClass = interfaceClass;
        this.environment = environment;

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        TurboProperties properties = BinderUtils.bind(environment, "spring.turbo", TurboProperties.class);

        // 暂时实现turbo协议
        Procotol procotol = properties.getProtocol().equalsIgnoreCase("turbo") ? new TurboProcotol() : new TurboProcotol();

        //服务接口名称
        String serviceKey = interfaceClass.getName();
        //获取某个接口的服务提供者列表
        RegisterCenter4Consumer registerCenter4Consumer = RegisterCenter.getInstance(properties.getZkServer());
        List<RegisterInfoProvider> providerServices = registerCenter4Consumer.getServiceMetaDataMap4Consumer().get(serviceKey);
        //根据软负载策略,从服务提供者列表选取本次调用的服务提供者
        String stragety = properties.getStragety();

        if(StringUtils.isEmpty(stragety)){
            stragety = LoadBalanceEnum.Random.getDesc();
        }
        LoadStrategy loadStrategyService = LoadBalanceEngine.queryLoadStrategy(stragety);
        RegisterInfoProvider serviceProvider = loadStrategyService.select(providerServices);
        URL url = new URL(serviceProvider.getIp(),serviceProvider.getPort());
        String impl = serviceProvider.getServiceObject().toString();
        int timeout = 20000;
        TurboRequest invocation = new TurboRequest(UUID.randomUUID().toString(),interfaceClass.getName(),method.getName(),args, method.getParameterTypes(),impl,timeout);
        Object res = procotol.send(url, invocation);
        return res;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment)environment;
    }
}
