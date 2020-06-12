package ds.young.turbo.protocol.turbo;

import ds.young.turbo.common.TurboRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<TurboRequest> {

	private ChannelHandlerContext context;


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TurboRequest turboRequest) throws Exception {
		InvokeTask it = new InvokeTask(turboRequest,ctx);
		NettyServer.submit(it);
	}


	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception{
		this.context = ctx;		
	}
	
}
