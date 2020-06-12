package ds.young.turbo.protocol.loadbalance.impl;

import ds.young.turbo.common.RegisterInfoProvider;
import ds.young.turbo.protocol.loadbalance.LoadStrategy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class HashLoadStrategy implements LoadStrategy {
    @Override
    public RegisterInfoProvider select(List<RegisterInfoProvider> providers) {

        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String ip = addr.getHostAddress();
        //获取源地址对应的hashcode
        int hashCode = ip.hashCode();
        //获取服务列表大小
        int size = providers.size();

        return providers.get(hashCode % size);
    }
}
