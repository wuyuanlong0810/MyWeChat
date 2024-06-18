package com.mywechat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.mywechat.entity.config.Appconfig;
import com.mywechat.entity.constants.Constants;
import com.mywechat.entity.dto.MessageSendDto;
import com.mywechat.entity.dto.SysSettingDto;
import com.mywechat.entity.dto.TokenUserInfoDto;
import com.mywechat.entity.enums.*;
import com.mywechat.entity.po.*;
import com.mywechat.entity.query.*;
import com.mywechat.exception.BusinessException;
import com.mywechat.mappers.*;
import com.mywechat.redis.RedisComponent;
import com.mywechat.service.UserContactService;
import com.mywechat.websocket.ChannelContextUtils;
import com.mywechat.websocket.MessageHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.index.PathBasedRedisIndexDefinition;
import org.springframework.stereotype.Service;

import com.mywechat.entity.vo.PaginationResultVO;
import com.mywechat.service.GroupInfoService;
import com.mywechat.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * 业务接口实现
 */
@Service("groupInfoService")
public class GroupInfoServiceImpl implements GroupInfoService {

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private Appconfig appconfig;

    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;

    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private UserContactService userContactService;
    @Resource
    private UserInfoMapper<UserInfo,UserInfoQuery> userInfoMapper;

    @Resource
    @Lazy
    private GroupInfoService groupInfoService;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<GroupInfo> findListByParam(GroupInfoQuery param) {
        return this.groupInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(GroupInfoQuery param) {
        return this.groupInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<GroupInfo> list = this.findListByParam(param);
        PaginationResultVO<GroupInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(GroupInfo bean) {
        return this.groupInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<GroupInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<GroupInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(GroupInfo bean, GroupInfoQuery param) {
        StringTools.checkParam(param);
        return this.groupInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(GroupInfoQuery param) {
        StringTools.checkParam(param);
        return this.groupInfoMapper.deleteByParam(param);
    }

    /**
     * 根据GroupId获取对象
     */
    @Override
    public GroupInfo getGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.selectByGroupId(groupId);
    }

    /**
     * 根据GroupId修改
     */
    @Override
    public Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId) {
        return this.groupInfoMapper.updateByGroupId(bean, groupId);
    }

    /**
     * 根据GroupId删除
     */
    @Override
    public Integer deleteGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.deleteByGroupId(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) {
        Date curDate = new Date();
        // 新增
        if (StringTools.isEmpty(groupInfo.getGroupId())) {
            GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
            groupInfoQuery.setGroupOwnerId(groupInfo.getGroupOwnerId());
            Integer count = groupInfoMapper.selectCount(groupInfoQuery);

            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            if (count >= sysSettingDto.getMaxGroupCount()) {
                throw new BusinessException("最多支持能创建" + sysSettingDto.getMaxGroupCount() + "个群组");
            }

            if (avatarFile == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }

            groupInfo.setCreateTime(curDate);
            groupInfo.setGroupId(StringTools.getGroupId());
            groupInfoMapper.insert(groupInfo);

            // 将群组添加为联系人
            UserContact userContact = new UserContact();
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setCreateTime(curDate);
            userContact.setLastUpdateTime(curDate);

            userContactMapper.insert(userContact);
            //创建会话
            String sessionId = StringTools.getChatSessionId4Group(groupInfo.getGroupId());
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSession.setLastReceiveTime(curDate.getTime());
            this.chatSessionMapper.insertOrUpdate(chatSession);

            // 添加会话用户
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setUserId(groupInfo.getGroupOwnerId());
            chatSessionUser.setContactId(groupInfo.getGroupId());
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUser.setSessionId(sessionId);
            this.chatSessionUserMapper.insert(chatSessionUser);

            // 创建消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.GROUP_CREATE.getType());
            chatMessage.setMessageContent(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatMessage.setSendTime(curDate.getTime());
            chatMessage.setContactId(groupInfo.getGroupId());
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
            chatMessageMapper.insert(chatMessage);

            // 将群组添加到联系人
            redisComponent.addUserContact(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());

            // 将联系人通道添加到群组通道
            channelContextUtils.addUser2Group(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());

            //发送ws消息
            chatSessionUser.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSessionUser.setLastReceiveTime(curDate.getTime());
            chatSessionUser.setMemberCount(1);
            // 创建并发送消息
            MessageSendDto messageSendDto = new MessageSendDto();
            BeanUtils.copyProperties(chatMessage, messageSendDto);
            messageSendDto.setExtendData(chatSessionUser);
            messageSendDto.setLastMessage(chatSessionUser.getLastMessage());
            messageHandler.sendMessage(messageSendDto);


        } else {
            GroupInfo dbInfo = groupInfoMapper.selectByGroupId(groupInfo.getGroupId());
            if (!dbInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            groupInfoMapper.updateByGroupId(groupInfo, groupInfo.getGroupId());

            //更新相关表冗余信息
            String contactNameUpdate = null;

            if (!dbInfo.getGroupName().equals(groupInfo.getGroupName())) {
                contactNameUpdate = groupInfo.getGroupName();
            }
            // 如果新的群组名称为空，直接返回
            if (contactNameUpdate == null) {
                return;
            }

            // 更新 ChatSessionUser 中的群组名称
            ChatSessionUser updateInfo = new ChatSessionUser();
            updateInfo.setContactName(groupInfo.getGroupName());
            ChatSessionUserQuery chatSessionUserQuery = new ChatSessionUserQuery();
            chatSessionUserQuery.setContactId(groupInfo.getGroupId());
            this.chatSessionUserMapper.updateByParam(updateInfo, chatSessionUserQuery);
            //修改群昵称发送 WebSocket 消息
            // 构建通知消息
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setContactType(UserContactTypeEnum.GROUP.getType());
            messageSendDto.setContactId(groupInfo.getGroupId());
            messageSendDto.setExtendData(contactNameUpdate);
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
            messageHandler.sendMessage(messageSendDto);

        }

        if (null==avatarFile){
            return;
        }
        String baseFolder = appconfig.getProjectFolder() + Constants.FILE_FOLDER;
        File targetFileFolder = new File(baseFolder + Constants.AVATAR_FOLDER);
        if (!targetFileFolder.exists()) {
            targetFileFolder.mkdirs();
        }
        String filePath = targetFileFolder.getPath() + "/" + groupInfo.getGroupId();


        try {
            avatarFile.transferTo(new File(filePath+ Constants.IMAGE_SUFFIX));
            avatarCover.transferTo(new File(filePath + Constants.COVER_IMAGE));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dissolutionGroup(String groupOwnerId, String groupId) {
        GroupInfo dbInfo = this.groupInfoMapper.selectByGroupId(groupId);
        if (dbInfo == null || !dbInfo.getGroupOwnerId().equals(groupOwnerId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //删除群组
        GroupInfo updateInfo = new GroupInfo();
        updateInfo.setStatus(GroupStatusEnum.DISSOLUTION.getStatus());
        this.groupInfoMapper.updateByGroupId(updateInfo, groupId);

        //更新联系人信息
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());
        UserContact updateUserContact = new UserContact();
        updateUserContact.setStatus(UserContactStatusEnum.DEL.getStatus());
        this.userContactMapper.updateByParam(updateUserContact, userContactQuery);

        //移除相关群员的联系人缓存
        List<UserContact> userContactList = userContactMapper.selectList(userContactQuery);
        for (UserContact userContact : userContactList) {
            redisComponent.removeUserContact(userContact.getUserId(),userContact.getContactId());
        }

        //1. 更新会话消息, 2. 记录群消息, 3. 发送解散群的通知
        String sessionId = StringTools.getChatSessionId4Group(groupId);
        Date curDate = new Date();
        String messageContent = MessageTypeEnum.DISSOLUTION_GROUP.getInitMessage();

        // 更新会话信息
        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(messageContent);
        chatSession.setLastReceiveTime(curDate.getTime());
        chatSessionMapper.updateBySessionId(chatSession, sessionId);

        // 创建并记录消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendTime(curDate.getTime());
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
        chatMessage.setMessageType(MessageTypeEnum.DISSOLUTION_GROUP.getType());
        chatMessage.setContactId(groupId);
        chatMessage.setMessageContent(messageContent);
        chatMessageMapper.insert(chatMessage);

        // 发送消息
        MessageSendDto messageSendDto = new MessageSendDto();
        BeanUtils.copyProperties(chatMessage, messageSendDto);
        messageHandler.sendMessage(messageSendDto);

    }

    @Override
    public void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String contactIds, Integer opType) {
        GroupInfo groupInfo = groupInfoMapper.selectByGroupId(groupId);
        if (groupInfo == null || !groupInfo.getGroupOwnerId().equals(tokenUserInfoDto.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String[] contactIdList = contactIds.split(",");
        for (String contactId : contactIdList) {
            if (AddOrRemoveTypeEnum.REMOVE.getType().equals(opType)) {
                groupInfoService.leaveGroup(contactId, groupId, MessageTypeEnum.REMOVE_GROUP);
            } else {
                userContactService.addContact(contactId, null, groupId, UserContactTypeEnum.GROUP.getType(), null);
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum) {
        GroupInfo groupInfo = groupInfoMapper.selectByGroupId(groupId);
        if (groupInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (userId.equals(groupInfo.getGroupOwnerId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        Integer count = userContactMapper.deleteByUserIdAndContactId(userId, groupId);
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        String sessionId = StringTools.getChatSessionId4Group(groupId);
        Date curDate = new Date();
        String messageContent = String.format(messageTypeEnum.getInitMessage(), userInfo.getNickName());

        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(messageContent);
        chatSession.setLastReceiveTime(curDate.getTime());
        chatSessionMapper.updateBySessionId(chatSession, sessionId);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendTime(curDate.getTime());
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
        chatMessage.setMessageType(messageTypeEnum.getType());
        chatMessage.setContactId(groupId);
        chatMessage.setMessageContent(messageContent);
        chatMessageMapper.insert(chatMessage);


        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        Integer memberCount = this.userContactMapper.selectCount(userContactQuery);
        MessageSendDto messageSendDto = new MessageSendDto();
        BeanUtils.copyProperties(chatMessage, messageSendDto);
        messageSendDto.setExtendData(userId);
        messageSendDto.setMemberCount(memberCount);
        messageHandler.sendMessage(messageSendDto);

    }

}