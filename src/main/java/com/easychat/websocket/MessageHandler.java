package com.easychat.websocket;

/**
 * @Author: 吴远龙
 * @Date: 2024-06-01 23:20
 */
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.redisson.api.RedissonClient;
import org.redisson.api.RTopic;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component("messageHandler")
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private static final String MESSAGE_TOPIC = "message.topic";

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @PostConstruct
    public void listenMessage() {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.addListener(MessageSendDto.class, (MessageSendDto, sendDto) -> {
            logger.info("收到广播消息: {}", JsonUtils.convertObj2Json(sendDto));
            channelContextUtils.sendMessage(sendDto);
        });
    }

    public void sendMessage(MessageSendDto sendDto) {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.publish(sendDto);
    }
}
