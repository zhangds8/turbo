package ds.young.turbo.protocol.loadbalance.impl;



import ds.young.turbo.common.RegisterInfoProvider;
import ds.young.turbo.protocol.loadbalance.LoadStrategy;

import java.util.List;
import java.util.Random;

public class RandomLoadStrategy implements LoadStrategy {
    @Override
    public RegisterInfoProvider select(List<RegisterInfoProvider> providers) {
        int m = providers.size();
        Random r = new Random();
        int index = r.nextInt(m);
        return providers.get(index);
    }
}
