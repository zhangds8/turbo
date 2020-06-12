package ds.young.turbo.protocol.loadbalance.impl;


import ds.young.turbo.common.RegisterInfoProvider;
import ds.young.turbo.protocol.loadbalance.LoadStrategy;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PollLoadStrategy implements LoadStrategy {

    //轮询算法

    //计数器
    private int index = 0;
    private Lock lock = new ReentrantLock();


    @Override
    public RegisterInfoProvider select(List<RegisterInfoProvider> providers) {
        RegisterInfoProvider service = null;
        try {
            lock.tryLock(10, TimeUnit.MILLISECONDS);
            //若计数大于服务提供者个数,将计数器归0
            if (index >= providers.size()) {
                index = 0;
            }
            service = providers.get(index);
            index++;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        //兜底,保证程序健壮性,若未取到服务,则直接取第一个
        if (service == null) {
            service = providers.get(0);
        }
        return service;
    }
}
