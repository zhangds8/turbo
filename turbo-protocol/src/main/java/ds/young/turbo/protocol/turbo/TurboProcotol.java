package ds.young.turbo.protocol.turbo;


import ds.young.turbo.common.MessageCallBack;
import ds.young.turbo.common.TurboRequest;
import ds.young.turbo.common.URL;
import ds.young.turbo.protocol.Procotol;
import ds.young.turbo.protocol.turbo.channelpool.NettyChannelPoolFactory;
import ds.young.turbo.protocol.turbo.channelpool.ResponseHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TurboProcotol implements Procotol {
    @Override
    public void start(URL url) {
        NettyServer nettyServer = NettyServer.getInstance();
        nettyServer.start(url.getHost(),url.getPort());
    }

    @Override
    public Object send(URL url, TurboRequest invocation) {
        ArrayBlockingQueue<Channel> queue = NettyChannelPoolFactory.getInstance().acqiure(url);
        Channel channel = null;
        try {
            channel = queue.poll(invocation.getTimeout(), TimeUnit.MILLISECONDS);
            if(channel == null || !channel.isActive() || !channel.isOpen()|| !channel.isWritable()){
                channel = queue.poll(invocation.getTimeout(), TimeUnit.MILLISECONDS);
                if(channel == null){
                    channel = NettyChannelPoolFactory.getInstance().registerChannel(url);
                }
            }
            //将本次调用的信息写入Netty通道,发起异步调用
            ChannelFuture channelFuture = channel.writeAndFlush(invocation);
            channelFuture.syncUninterruptibly();
            MessageCallBack callback = new MessageCallBack(invocation);
            ResponseHolder.getInstance().mapCallBack.put(invocation.getRequestId(), callback);
            try {
                return callback.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }finally{
            NettyChannelPoolFactory.getInstance().release(queue, channel, url);
        }
        return null;
    }
}
