package ds.young.turbo.protocol.turbo;

import ds.young.turbo.common.Configuration;
import ds.young.turbo.common.TurboResponse;
import ds.young.turbo.protocol.serializer.NettyDecoderHandler;
import ds.young.turbo.protocol.serializer.NettyEncoderHandler;
import ds.young.turbo.protocol.serializer.SerializeType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


public class NettyClient {

	private static NettyClient INSTANCE = new NettyClient();

	private final static int parallel = Runtime.getRuntime().availableProcessors() * 2;

	private NettyClient(){};

	public static NettyClient getInstance(){
		return INSTANCE;
	}

	private SerializeType serializeType = SerializeType.queryByType("java");

	public void start(String host,Integer port){

		Bootstrap bootstrap = new Bootstrap();
		EventLoopGroup group = new NioEventLoopGroup(parallel);

		try{
			bootstrap.group(group)
					.channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>(){

						@Override
						protected void initChannel(SocketChannel arg0) throws Exception {
							ChannelPipeline pipeline = arg0.pipeline();
							pipeline.addLast(new NettyEncoderHandler(serializeType));
							//注册Netty解码器
							pipeline.addLast(new NettyDecoderHandler(TurboResponse.class, serializeType));
							pipeline.addLast("handler", new NettyClientHandler());

						}

					});
			ChannelFuture future = bootstrap.connect(host,port).sync();
		}catch(Exception e){
			group.shutdownGracefully();
		}


	}



}
