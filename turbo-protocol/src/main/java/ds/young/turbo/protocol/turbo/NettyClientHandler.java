package ds.young.turbo.protocol.turbo;

import ds.young.turbo.common.MessageCallBack;
import ds.young.turbo.common.TurboResponse;
import ds.young.turbo.protocol.turbo.channelpool.ResponseHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class NettyClientHandler extends SimpleChannelInboundHandler<TurboResponse> {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    private ChannelHandlerContext context;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("[ TURBO ] 停止时间是：" + new Date());
        logger.info("[ TURBO ] HeartBeatClientHandler channelInactive");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
        logger.info("[ TURBO ] 激活时间是："+ctx.channel().id());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TurboResponse turboResponse) throws Exception {
        String responseId = turboResponse.getResponseId();
        MessageCallBack callBack = ResponseHolder.getInstance().mapCallBack.get(responseId);
        if(callBack != null){
            ResponseHolder.getInstance().mapCallBack.remove(responseId);
            callBack.over(turboResponse);
        }
    }
}
