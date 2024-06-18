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
import com.mywechat.entity.po.ChatSession;
import com.mywechat.entity.po.UserContact;
import com.mywechat.entity.query.ChatSessionQuery;
import com.mywechat.entity.query.UserContactQuery;
import com.mywechat.exception.BusinessException;
import com.mywechat.mappers.ChatSessionMapper;
import com.mywechat.mappers.UserContactMapper;
import com.mywechat.redis.RedisComponent;
import com.mywechat.utils.DateUtil;
import com.mywechat.websocket.MessageHandler;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.mywechat.entity.query.ChatMessageQuery;
import com.mywechat.entity.po.ChatMessage;
import com.mywechat.entity.vo.PaginationResultVO;
import com.mywechat.entity.query.SimplePage;
import com.mywechat.mappers.ChatMessageMapper;
import com.mywechat.service.ChatMessageService;
import com.mywechat.utils.StringTools;
import org.springframework.web.multipart.MultipartFile;


/**
 * 聊天消息表 业务接口实现
 */
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {
    public static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;

    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private Appconfig appconfig;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<ChatMessage> findListByParam(ChatMessageQuery param) {
        return this.chatMessageMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(ChatMessageQuery param) {
        return this.chatMessageMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ChatMessage> list = this.findListByParam(param);
        PaginationResultVO<ChatMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ChatMessage bean) {
        return this.chatMessageMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(ChatMessage bean, ChatMessageQuery param) {
        StringTools.checkParam(param);
        return this.chatMessageMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(ChatMessageQuery param) {
        StringTools.checkParam(param);
        return this.chatMessageMapper.deleteByParam(param);
    }

    /**
     * 根据MessageId获取对象
     */
    @Override
    public ChatMessage getChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.selectByMessageId(messageId);
    }

    /**
     * 根据MessageId修改
     */
    @Override
    public Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId) {
        return this.chatMessageMapper.updateByMessageId(bean, messageId);
    }

    /**
     * 根据MessageId删除
     */
    @Override
    public Integer deleteChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.deleteByMessageId(messageId);
    }

    @Override
    public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
        String contactId = chatMessage.getContactId();

        // 不是机器人回复，判断好友状态
        if (!Constants.ROBOT_UID.equals(tokenUserInfoDto.getUserId())) {
            List<String> contactList = redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
            if (!contactList.contains(contactId)) {
                UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
                if (UserContactTypeEnum.USER == userContactTypeEnum) {
                    throw new BusinessException(ResponseCodeEnum.CODE_902);
                } else {
                    throw new BusinessException(ResponseCodeEnum.CODE_903);
                }
            }
        }

        String sessionId = null;
        String sendUserId = tokenUserInfoDto.getUserId();

        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);

        if (UserContactTypeEnum.USER == contactTypeEnum) {
            sessionId = StringTools.getChatSessionId4User(new String[]{sendUserId, contactId});
        } else {
            sessionId = StringTools.getChatSessionId4Group(contactId);
        }
        chatMessage.setSessionId(sessionId);

        Long curTime = System.currentTimeMillis();
        chatMessage.setSendTime(curTime);

        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        Integer status = MessageTypeEnum.MEDIA_CHAT == messageTypeEnum ?
                MessageStatusEnum.SENDING.getStatus() :
                MessageStatusEnum.SENT.getStatus();
        chatMessage.setStatus(status);

        String messageContent = StringTools.cleanHtmlTag(chatMessage.getMessageContent());
        chatMessage.setMessageContent(messageContent);

        //更新会话
        ChatSession chatSession = new ChatSession();
        if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            chatSession.setLastMessage(tokenUserInfoDto.getNickName() + ": " + messageContent);
        } else {
            chatSession.setLastMessage(messageContent);
        }
        chatSession.setLastReceiveTime(curTime);
        chatSessionMapper.updateBySessionId(chatSession, sessionId);
        //记录消息表
        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
        chatMessage.setContactType(contactTypeEnum.getType());
        chatMessageMapper.insert(chatMessage);

        MessageSendDto messageSendDto = new MessageSendDto();
        BeanUtils.copyProperties(chatMessage, messageSendDto);

        if (Constants.ROBOT_UID.equals(contactId)) {
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            TokenUserInfoDto robot = new TokenUserInfoDto();
            robot.setUserId(sysSettingDto.getRobotUid());
            robot.setNickName(sysSettingDto.getRobotNickName());
            ChatMessage robotChatMessage = new ChatMessage();
            robotChatMessage.setContactId(sendUserId);
            //这里可以对接AI聊天
            robotChatMessage.setMessageContent("我只是一个机器人无法识别你的消息");
            robotChatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
            saveMessage(robotChatMessage, robot);
        } else {
            messageHandler.sendMessage(messageSendDto);
        }

        return messageSendDto;
    }

    @Override
    public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) {
        // 获取消息信息
        ChatMessage chatMessage = chatMessageMapper.selectByMessageId(messageId);
        if (chatMessage == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 检查用户是否是消息的发送者
        if (!chatMessage.getSendUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 获取系统设置
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        String fileSuffix = StringTools.getFileSuffix(file.getOriginalFilename());

        // 检查文件大小和类型
        if (!StringTools.isEmpty(fileSuffix)) {
            fileSuffix = fileSuffix.toLowerCase();
            if (ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix) && file.getSize() > sysSettingDto.getMaxImageSize() * Constants.FILE_SIZE_MB) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            } else if (ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix) && file.getSize() > sysSettingDto.getMaxVideoSize() * Constants.FILE_SIZE_MB) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            } else if (!ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix) && !ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix) && file.getSize() > sysSettingDto.getMaxFileSize() * Constants.FILE_SIZE_MB) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        } else {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String fileName = file.getOriginalFilename();
        String fileExtName = StringTools.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtName;
        String fileCoverName = messageId + Constants.COVER_IMAGE;
        String month = DateUtil.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appconfig.getProjectFolder() + Constants.FILE_FOLDER + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // 文件上传
        try {
            file.transferTo(new File(folder.getPath() + "/" + fileRealName));
            cover.transferTo(new File(folder.getPath() + "/" + fileCoverName));
        } catch (IOException e) {
            logger.error("上传文件失败", e);
            throw new BusinessException("文件上传失败");
        }

        // 更新消息状态
        ChatMessage uploadInfo = new ChatMessage();
        uploadInfo.setStatus(MessageStatusEnum.SENT.getStatus());
        ChatMessageQuery messageQuery = new ChatMessageQuery();
        messageQuery.setMessageId(messageId);
        chatMessageMapper.updateByParam(uploadInfo, messageQuery);

        // 发送消息通知
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setStatus(MessageStatusEnum.SENT.getStatus());
        messageSendDto.setMessageId(messageId);
        messageSendDto.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        messageSendDto.setContactId(chatMessage.getContactId());
        messageHandler.sendMessage(messageSendDto);
    }

    @Override
    public File downloadFile(TokenUserInfoDto token, Long messageId, Boolean showCover) {
        ChatMessage message = chatMessageMapper.selectByMessageId(messageId);
        String contactId = message.getContactId();
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);

        if (contactTypeEnum == UserContactTypeEnum.USER && !token.getUserId().equals(contactId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (contactTypeEnum == UserContactTypeEnum.GROUP) {
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setUserId(token.getUserId());
            userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer contactCount = userContactMapper.selectCount(userContactQuery);
            if (contactCount == 0) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        String month = DateUtil.format(new Date(message.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appconfig.getProjectFolder() + Constants.FILE_FOLDER + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = message.getFileName();
        String fileExtName = StringTools.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtName;
        if (showCover!=null&&showCover){
            fileRealName = messageId + Constants.COVER_IMAGE;
        }
        File file = new File(folder.getPath()+"/"+fileRealName);
        if (!file.exists()){
            logger.error("文件不存在{}",messageId);
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }
        logger.info("返回的文件名{}路径",file.getName(),file.getPath());
        return file;
    }
}