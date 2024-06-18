package com.mywechat.websocket.netty;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-29 22:53
 */

import com.mywechat.entity.dto.TokenUserInfoDto;
import com.mywechat.redis.RedisComponent;
import com.mywechat.utils.StringTools;
import com.mywechat.websocket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {//自定义socket处理
    private static final Logger logger = LoggerFactory.getLogger(HandlerWebSocket.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ChannelContextUtils channelContextUtils;

    /**
     * Called when a new connection is established.
     *
     * @param ctx the channel handler context
     * @throws Exception if any error occurs
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有新连接...");
//        super.channelActive(ctx);
    }

    /**
     * Called when a connection is closed.
     *
     * @param ctx the channel handler context
     * @throws Exception if any error occurs
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有连接断开...");
//        super.channelInactive(ctx);
        channelContextUtils.removeContext(ctx.channel());
    }

    /**
     * Called when a new text WebSocket frame is received.
     *
     * @param ctx   the channel handler context
     * @param frame the text WebSocket frame
     * @throws Exception if any error occurs
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String text = frame.text();

        // Echo the message back to the client
        Channel channel = ctx.channel();
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        logger.info("收到消息来自{}的消息: {}", userId, text);
        redisComponent.saveHeartBeat(userId);
    }

//    /**
//     * Called when an exception is caught.
//     *
//     * @param ctx   the channel handler context
//     * @param cause the throwable
//     * @throws Exception if any error occurs
//     */
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        logger.error("An error occurred: {}", cause.getMessage());
//        ctx.close();
//    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String url = complete.requestUri();
            String token = getToken(url);
            if (token == null) {
                ctx.channel().close();
                return;
            }

            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(token);
            if (tokenUserInfoDto == null) {
                ctx.channel().close();
                return;
            }
            channelContextUtils.addContext(tokenUserInfoDto.getUserId(), ctx.channel());
//            logger.info("url:{}", url);
        }
    }

    private String getToken(String url) {
        if (StringTools.isEmpty(url) || url.indexOf("?") == -1) {
            return null;
        }

        String[] queryParams = url.split("\\?");
        if (queryParams.length != 2) {
            return null;
        }

        String[] params = queryParams[1].split("=");
        if (params.length != 2) {
            return null;
        }

        return params[1];
    }

}

