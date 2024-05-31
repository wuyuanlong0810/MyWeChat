package com.easychat.websocket.netty;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-29 22:21
 */

import com.easychat.entity.config.Appconfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class NettyWebSocketStarter {
    private static final Logger logger = LoggerFactory.getLogger(NettyWebSocketStarter.class);

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();
    @Resource
    private HandlerWebSocket handlerWebSocket;
    @Resource
    private Appconfig appconfig;

    public void close(){
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Async
    public void startNetty(){
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            //设置几个重要参数
                            // 对http协议的支持，使用http编码器、解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 聚合解码 httpRequest/httpContent/lastHttpContent/fullHttpRequest
                            //保证接受的http请求的完整性
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            // 心跳 long readerIdleTime,long writerIdleTime, long allIdleTime, TimeUnit unit
                            // readerIdleTime 读超时时间 即测试段一定时间内未接收到被测试段消息
                            // writerIdleTime 为写超时时间 即测试端一定时间内未向被测试端发送消息
                            // a1lIdleTime 所有类型的超时时间
                            pipeline.addLast(new IdleStateHandler(30, 0, 0));
                            pipeline.addLast(new HandlerHeartBeat());
                            //将http协议升级为ws协议
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true,65536,true,true,10000L));
                            pipeline.addLast(handlerWebSocket);
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(appconfig.getPort()).sync();
            logger.info("netty启动成功，端口：{}",appconfig.getPort());
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("启动netty失败", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
