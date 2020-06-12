package ds.young.turbo.protocol.loadbalance;


import ds.young.turbo.common.RegisterInfoProvider;

import java.util.List;

/**
 * 负载均衡算法接口
 */
public interface LoadStrategy {

    public RegisterInfoProvider select(List<RegisterInfoProvider> providers);

}
