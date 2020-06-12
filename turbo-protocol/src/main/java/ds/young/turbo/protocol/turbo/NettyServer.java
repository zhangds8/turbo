package ds.young.turbo.protocol.turbo;

import ds.young.turbo.common.TurboRequest;
import ds.young.turbo.protocol.serializer.NettyDecoderHandler;
import ds.young.turbo.protocol.serializer.NettyEncoderHandler;
import ds.young.turbo.protocol.serializer.SerializeType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NettyServer {

	private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
	
	private static NettyServer INSTANCE = new NettyServer();
	
	private static Executor executor = Executors.newCachedThreadPool();
	
    private final static int MESSAGE_LENGTH = 4;
    
    private NettyServer(){};
    
    public static NettyServer getInstance(){
    	return INSTANCE;
    }


	private SerializeType serializeType = SerializeType.queryByType("java");
    
    public static void submit(Runnable t){
    	executor.execute(t);
    }
	
	public void start(String host, Integer port){
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try{
			final ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup,workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
			.childHandler(new ChannelInitializer<SocketChannel>(){

				@Override
				protected void initChannel(SocketChannel arg0) throws Exception {
					ChannelPipeline pipeline = arg0.pipeline();
					pipeline.addLast(new NettyDecoderHandler(TurboRequest.class, serializeType));
					//注册编码器NettyEncoderHandler
					pipeline.addLast(new NettyEncoderHandler(serializeType));
					pipeline.addLast("handler", new NettyServerHandler());
					
				}
				
			});
			bootstrap.bind(host, port).sync().channel();
			if(logger.isInfoEnabled()){
				logger.info("[ TURBO ] ||=============================||");
				logger.info("[ TURBO ] || netty已启动, 绑定端口：{}    ||", port);
				logger.info("[ TURBO ] ||=============================||");
			}
		}catch(Exception e){
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	

}
