package ds.young.turbo.protocol.turbo;

import ds.young.turbo.common.TurboRequest;
import ds.young.turbo.common.TurboResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Method;

public class InvokeTask implements Runnable{
	
	private TurboRequest invocation;
	private ChannelHandlerContext ctx;

	public InvokeTask(TurboRequest invocation,ChannelHandlerContext ctx) {
		super();
		this.invocation = invocation;
		this.ctx = ctx;
	}


	@Override
	public void run() {
		
        // 从注册中心根据接口，找接口的实现类
        String interFaceName = invocation.getInterfaceName();
		Class impClass = null;
		try {
			impClass = Class.forName(invocation.getImpl());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		Method method;
        Object result = null;
		try {
			method = impClass.getMethod(invocation.getMethodName(),invocation.getParamTypes());
			//这块考虑实现类，是不是应该在 spring 里面拿
	        result = method.invoke(impClass.newInstance(),invocation.getParams());
		} catch (Exception e) {
			e.printStackTrace();
		}
		TurboResponse rpcResponse = new TurboResponse();
		rpcResponse.setResponseId(invocation.getRequestId());
		rpcResponse.setData(result);
        ctx.writeAndFlush(rpcResponse).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
            }
        });

	}

}
