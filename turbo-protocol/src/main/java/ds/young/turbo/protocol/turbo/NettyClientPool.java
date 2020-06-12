package ds.young.turbo.protocol.turbo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyClientPool {

    public  static Map<String,NettyClientHandler> holder = new ConcurrentHashMap<>();
}
