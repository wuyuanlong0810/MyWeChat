package com.easychat.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @Author: 吴远龙
 * @Date: 2024-05-29 22:48
 */
public class HandlerHeartBeat extends ChannelDuplexHandler {//心跳检测
    private static final Logger logger = LoggerFactory.getLogger(HandlerHeartBeat.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                Channel channel = ctx.channel();
                Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
                String userId = attribute.get();
                logger.info("用户{}心跳超时",userId);
                ctx.close(); // Optionally close the connection
            } else if (e.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush("heartbeat");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
