package com.easychat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.PrimitiveIterator;

import javax.annotation.Resource;

import com.easychat.entity.config.Appconfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.*;
import com.easychat.entity.query.*;
import com.easychat.exception.BusinessException;
import com.easychat.mappers.*;
import com.easychat.redis.RedisComponent;
import com.easychat.websocket.ChannelContextUtils;
import com.easychat.websocket.MessageHandler;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.service.GroupInfoService;
import com.easychat.utils.StringTools;
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

        // TODO: Remove cached contacts for group members
        // TODO: Send messages - 1. Update conversation information, 2. Record group messages, 3. Send dissolution notification messages
    }



}