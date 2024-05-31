package com.easychat.websocket;

import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.WsInitData;
import com.easychat.entity.enums.UserContactApplyStatusEnum;
import com.easychat.entity.enums.UserContactTypeEnum;
import com.easychat.entity.enums.MessageTypeEnum;
import com.easychat.entity.po.ChatMessage;
import com.easychat.entity.po.ChatSessionUser;
import com.easychat.entity.po.UserContactApply;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.query.ChatMessageQuery;
import com.easychat.entity.query.ChatSessionUserQuery;
import com.easychat.entity.query.UserContactApplyQuery;
import com.easychat.entity.query.UserInfoQuery;
import com.easychat.mappers.ChatMessageMapper;
import com.easychat.mappers.UserContactApplyMapper;
import com.easychat.mappers.UserInfoMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.service.ChatSessionUserService;
import com.easychat.utils.JsonUtils;
import com.easychat.utils.StringTools;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-30 16:38
 */
@Component
public class ChannelContextUtils {
    private static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private ChatMessageMapper<ChatMessage,ChatMessageQuery> chatMessageMapper;

    @Resource
    private UserContactApplyMapper<UserContactApply,UserContactApplyQuery> userContactApplyMapper;

    public void addContext(String userId, Channel channel) {
        String channelId = channel.id().toString();
        AttributeKey<String> attributeKey = null;
        if (AttributeKey.exists(channelId)) {
            attributeKey = AttributeKey.newInstance(channelId);
        } else {
            attributeKey = AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);

        //将群组加入到channelGroup
        List<String> userContactList = redisComponent.getUserContactList(userId);
        for (String groupId : userContactList) {
            if (groupId.startsWith(UserContactTypeEnum.GROUP.getPrefix())) {
                add2Group(groupId, channel);
            }
        }
        //将用户加入到channel
        USER_CONTEXT_MAP.put(userId, channel);
        redisComponent.saveHeartBeat(userId);

        //更新用户最后连接时间
        UserInfo updateInfo = new UserInfo();
        updateInfo.setLastLoginTime(new Date());
        userInfoMapper.updateByUserId(updateInfo, userId);

        //给用户发送消息
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        Long sourceLastOffTime = userInfo.getLastOffTime();
        Long lastOffTime = sourceLastOffTime;//最多查距离上次登录3天的消息
        if (lastOffTime != null && System.currentTimeMillis() - Constants.MILLISSECONDS_3DAYS_AGO > sourceLastOffTime) {
            lastOffTime = Constants.MILLISSECONDS_3DAYS_AGO;
        }

        /**
         * 查询用户所有会话信息，保证换设备后会话同步
         */
        ChatSessionUserQuery sessionUserQuery = new ChatSessionUserQuery();
        sessionUserQuery.setUserId(userId);
        sessionUserQuery.setOrderBy("last_receive_time desc");
        List<ChatSessionUser> chatSessionUserList = chatSessionUserService.findListByParam(sessionUserQuery);

        WsInitData wsInitData = new WsInitData();
        wsInitData.setChatSessionList(chatSessionUserList);
        /**
         * 查询聊天信息
         */
        //获得所有的联系人
        List<String> groupIdList = userContactList.stream().filter(item -> item.startsWith(UserContactTypeEnum.GROUP.getPrefix())).collect(Collectors.toList());
        groupIdList.add(userId);
        ChatMessageQuery chatMessageQuery = new ChatMessageQuery();
        chatMessageQuery.setContactList(groupIdList);
        chatMessageQuery.setLastReceiveTime(lastOffTime);

        List<ChatMessage> chatMessageList = chatMessageMapper.selectList(chatMessageQuery);

        wsInitData.setChatMessageList(chatMessageList);
        /**
         * 查询好友申请
         */
        UserContactApplyQuery userContactApplyQuery = new UserContactApplyQuery();
        userContactApplyQuery.setReceiveUserId(userId);
        userContactApplyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
        userContactApplyQuery.setLastApplyTimestamp(lastOffTime);
        Integer i = userContactApplyMapper.selectCount(userContactApplyQuery);
        wsInitData.setApplyCount(i);
        //发送消息
        MessageSendDto messageSendDto= new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setContactId(userId);
        messageSendDto.setExtendData(wsInitData);

        sendMsg(messageSendDto,userId);

    }

    //发送消息
    public static void sendMsg(MessageSendDto messageSendDto, String receiveId) {
        if (receiveId == null) {
            return;
        }
        Channel sendChannel = USER_CONTEXT_MAP.get(receiveId);
        if (sendChannel == null) {
            return;
        }
        // 相对于客户端而言，联系人就是发送人，所以要转一下再发送
        messageSendDto.setContactId(messageSendDto.getSendUserId());
        messageSendDto.setContactName(messageSendDto.getSendUserNickName());
        sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));
    }



    private void add2Group(String groupId, Channel channel) {
        // Retrieve or create the ChannelGroup
        ChannelGroup group = GROUP_CONTEXT_MAP.computeIfAbsent(groupId, id ->
                new DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
        );

        // Add the channel to the group if it's not already present
        if (!group.contains(channel)) {
            group.add(channel);
        }
    }

    public void removeContext(Channel channel) {
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        if (!StringTools.isEmpty(userId)) {
            USER_CONTEXT_MAP.remove(userId);
        }
        redisComponent.removeHeartBeat(userId);

        //更新用户最后离线时间
        UserInfo userInfo = new UserInfo();
        userInfo.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.updateByUserId(userInfo, userId);
    }

}
