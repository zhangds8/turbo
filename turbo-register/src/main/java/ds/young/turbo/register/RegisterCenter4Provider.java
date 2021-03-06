package ds.young.turbo.register;


import ds.young.turbo.common.RegisterInfoProvider;

import java.util.List;
import java.util.Map;

public interface RegisterCenter4Provider {
    /**
     * 服务端将服务提供者信息注册到 zookeeper 对应的节点下
     * @param serivceList
     */
    public void registerProvider(final List<RegisterInfoProvider> serivceList);

    /**
     * 服务端获取服务提供者信息
     * @return key：服务提供者接口 value：服务提供者服务方法列表
     */
    public Map<String, List<RegisterInfoProvider>> getProviderService();
}
