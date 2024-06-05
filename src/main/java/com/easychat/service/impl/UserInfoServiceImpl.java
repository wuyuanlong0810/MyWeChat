package com.easychat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.easychat.entity.config.Appconfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.ChatSessionUser;
import com.easychat.entity.po.UserContact;
import com.easychat.entity.po.UserInfoBeauty;
import com.easychat.entity.query.ChatSessionUserQuery;
import com.easychat.entity.query.UserContactQuery;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.exception.BusinessException;
import com.easychat.mappers.ChatSessionUserMapper;
import com.easychat.mappers.UserContactMapper;
import com.easychat.mappers.UserInfoBeautyMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.service.UserContactService;
import com.easychat.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.easychat.entity.query.UserInfoQuery;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.query.SimplePage;
import com.easychat.mappers.UserInfoMapper;
import com.easychat.service.UserInfoService;
import com.easychat.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private UserInfoBeautyMapper<UserInfoBeauty, UserInfoQuery> userInfoBeautyMapper;

    @Resource
    private UserContactMapper<UserContact,UserContactQuery> userContactMapper;


    @Resource
    private Appconfig appconfig;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserContactService userContactService;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserInfo> findListByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(param);
        PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
        StringTools.checkParam(param);
        return this.userInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserInfoQuery param) {
        StringTools.checkParam(param);
        return this.userInfoMapper.deleteByParam(param);
    }

    /**
     * 根据UserId获取对象
     */
    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId修改
     */
    @Override
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    @Override
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email获取对象
     */
    @Override
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email修改
     */
    @Override
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    @Override
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }

    /**
     * 注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String password, String nickName) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);

        if (userInfo != null) {
            throw new BusinessException("邮箱已注册");
        }
        String userId = StringTools.getUserId();
        UserInfoBeauty beautyAccount = userInfoBeautyMapper.selectByEmail(email);
        if (beautyAccount != null) {
            Boolean used = beautyAccount.getStatus() == BeautyAccountStatusEnum.USED.getStatus();
            if (!used) {
                userId = UserContactTypeEnum.USER.getPrefix() + beautyAccount.getUserId();
                UserInfoBeauty userInfoBeauty = new UserInfoBeauty();
                userInfoBeauty.setStatus(BeautyAccountStatusEnum.USED.getStatus());
                userInfoBeautyMapper.updateById(userInfoBeauty, beautyAccount.getId());
            }
        }
        Date curDate = new Date();
        UserInfo userInfo1 = new UserInfo();
        userInfo1.setUserId(userId);
        userInfo1.setEmail(email);
        userInfo1.setNickName(nickName);
        userInfo1.setPassword(StringTools.encodeMD5(password));
        userInfo1.setCreateTime(curDate);
        userInfo1.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo1.setLastOffTime(curDate.getTime());
        userInfo1.setJoinType(JoinTypeEnum.APPLY.getType());

        userInfoMapper.insert(userInfo1);

        //创建机器人好友

        userContactService.addContact4Robot(userId);

    }

    @Override
    public UserInfoVO login(String email, String password) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if (userInfo == null) {
            throw new BusinessException("账号不存在");
        }
        if (!userInfo.getPassword().equals(password)) {
            throw new BusinessException("密码错误");
        }
        if (userInfo.getStatus().equals(UserStatusEnum.DISABLE.getStatus())) {
            throw new BusinessException("账号被禁用");
        }
        Long userHeartBeat = redisComponent.getUserHeartBeat(userInfo.getUserId());
        if(userHeartBeat!=null){
            throw new BusinessException("此账号已在别处登录");
        }

        UserContactQuery contactQuery = new UserContactQuery();
        contactQuery.setUserId(userInfo.getUserId());
        contactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());

        // 查询联系人列表
        List<UserContact> contactList = userContactMapper.selectList(contactQuery);

        // 提取联系人ID列表
        List<String> contactIdList = contactList.stream()
                .map(UserContact::getContactId)
                .collect(Collectors.toList());

        // 清空Redis中的联系人信息
        redisComponent.cleanUserContact(userInfo.getUserId());

        // 批量添加新的联系人信息到Redis
        if (!contactIdList.isEmpty()) {
            redisComponent.addUserContactBatch(userInfo.getUserId(), contactIdList);
        }

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(userInfo);

        String token = StringTools.encodeMD5(tokenUserInfoDto.getUserId()+StringTools.getRandomString(20));
        tokenUserInfoDto.setToken(token);

        //保存登录信息到redis
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);


        UserInfoVO userInfoVO = new UserInfoVO();

        BeanUtils.copyProperties(userInfo,userInfoVO);

        userInfoVO.setToken(tokenUserInfoDto.getToken());
        userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());


        return userInfoVO;
    }

    private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo){
        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        tokenUserInfoDto.setNickName(userInfo.getNickName());
        tokenUserInfoDto.setUserId(userInfo.getUserId());

        String adminEmails = appconfig.getAdminEmails();
        if (!StringTools.isEmpty(adminEmails) && ArrayUtils.contains(adminEmails.split(","), userInfo.getEmail())) {
            tokenUserInfoDto.setAdmin(true);
        } else {
            tokenUserInfoDto.setAdmin(false);
        }
        return tokenUserInfoDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        if (avatarFile != null) {
            String baseFolder = appconfig.getProjectFolder() + Constants.FILE_FOLDER;
            File targetFileFolder = new File(baseFolder + Constants.AVATAR_FOLDER);
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + userInfo.getUserId();
            avatarFile.transferTo(new File(filePath + Constants.IMAGE_SUFFIX));
            avatarCover.transferTo(new File(filePath + Constants.COVER_IMAGE));
        }

        UserInfo dbInfo = this.userInfoMapper.selectByUserId(userInfo.getUserId());
        this.userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());

        String contactNameUpdate = null;
        if (!dbInfo.getNickName().equals(userInfo.getNickName())) {
            contactNameUpdate = userInfo.getNickName();
        }
        if (contactNameUpdate==null){
            return;
        }

        //更新redis中的token中的昵称
        TokenUserInfoDto tokenUserInfoDtoByUserId = redisComponent.getTokenUserInfoDtoByUserId(userInfo.getUserId());
        tokenUserInfoDtoByUserId.setNickName(contactNameUpdate);
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDtoByUserId);


        //Update conversation information with the new nickname
        // 更新 ChatSessionUser 中的用户名称
        ChatSessionUser updateInfo = new ChatSessionUser();
        updateInfo.setContactName(contactNameUpdate);
        ChatSessionUserQuery chatSessionUserQuery = new ChatSessionUserQuery();
        chatSessionUserQuery.setContactId(userInfo.getUserId());
        this.chatSessionUserMapper.updateByParam(updateInfo, chatSessionUserQuery);
        //修改昵称发送 WebSocket 消息
        // 创建 UserContactQuery 实例并设置查询参数
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactType(UserContactTypeEnum.USER.getType());
        userContactQuery.setContactId(userInfo.getUserId());
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());

        // 根据查询结果获取相关用户列表
        List<UserContact> userContactList = userContactMapper.selectList(userContactQuery);

        // 遍历用户列表，构建消息并发送
        for (UserContact userContact : userContactList) {
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setContactType(UserContactTypeEnum.USER.getType());
            messageSendDto.setContactId(userContact.getUserId());
            messageSendDto.setExtendData(contactNameUpdate);
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
            messageSendDto.setSendUserId(userInfo.getUserId());
            messageSendDto.setSendUserNickName(contactNameUpdate);
            messageHandler.sendMessage(messageSendDto);
        }
    }

    @Override
    public void updateUserStatus(Integer status, String userId) {
        UserStatusEnum userStatusEnum = UserStatusEnum.getByStatus(status);

        if (userStatusEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setStatus(userStatusEnum.getStatus());

        this.userInfoMapper.updateByUserId(userInfo, userId);
    }

    @Override
    public void forceOffLine(String UserId) {

    }
}