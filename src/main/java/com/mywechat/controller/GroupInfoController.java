package com.mywechat.controller;

import java.util.List;

import com.mywechat.annotation.GlobalInterceptor;
import com.mywechat.entity.dto.TokenUserInfoDto;
import com.mywechat.entity.enums.GroupStatusEnum;
import com.mywechat.entity.enums.MessageTypeEnum;
import com.mywechat.entity.enums.UserContactStatusEnum;
import com.mywechat.entity.po.UserContact;
import com.mywechat.entity.query.GroupInfoQuery;
import com.mywechat.entity.po.GroupInfo;
import com.mywechat.entity.query.UserContactQuery;
import com.mywechat.entity.vo.GroupInfoVo;
import com.mywechat.entity.vo.ResponseVO;
import com.mywechat.exception.BusinessException;
import com.mywechat.service.GroupInfoService;
import com.mywechat.service.UserContactService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Controller
 */
@RestController("groupInfoController")
@RequestMapping("/group")
public class GroupInfoController extends ABaseController {//群组信息控制层

    @Resource
    private GroupInfoService groupInfoService;

    @Resource
    private UserContactService userContactService;

    /**
     * 创建或修改群
     * @param request
     * @param groupId
     * @param groupName
     * @param groupNotice
     * @param joinType
     * @param avatarFile
     * @param avatarCover
     * @return
     */
    @RequestMapping("/saveGroup")
//    @GlobalInterceptor
    public ResponseVO saveGroup(HttpServletRequest request,String groupId,
                                @NotEmpty String groupName,
                                String groupNotice,
                                @NotNull Integer joinType,
                                MultipartFile avatarFile,//群组的头像
                                MultipartFile avatarCover) {//群组的头像缩略图

        TokenUserInfoDto token = getToken(request);
        GroupInfo groupInfo = new GroupInfo();

        groupInfo.setGroupId(groupId);
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        groupInfo.setJoinType(joinType);
        groupInfo.setGroupOwnerId(token.getUserId());

        groupInfoService.saveGroup(groupInfo,avatarFile,avatarCover);

        return getSuccessResponseVO(null);
    }

    /**
     * 获取我创建的群
     * @param request
     * @return
     */
    @RequestMapping("/loadMyGroup")
    //@GlobalInterceptor
    public ResponseVO loadMyGroup(HttpServletRequest request) {

        TokenUserInfoDto tokenUserInfoDto = getToken(request);
        GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
        groupInfoQuery.setGroupOwnerId(tokenUserInfoDto.getUserId());
        groupInfoQuery.setOrderBy("create_time DESC");
        List<GroupInfo> groupInfoList = this.groupInfoService.findListByParam(groupInfoQuery);
        return getSuccessResponseVO(groupInfoList);

    }

    /**
     * 获取群聊的详情
     * @param request
     * @param groupId
     * @return
     */
    @RequestMapping("/getGroupInfo")
    //@GlobalInterceptor
    public ResponseVO getGroupInfo(HttpServletRequest request, @NotEmpty String groupId) {
        GroupInfo groupInfo = getGroupDetailCommon(request, groupId);
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        Integer memberCount = this.userContactService.findCountByParam(userContactQuery);
        groupInfo.setMemberCount(memberCount);

        return getSuccessResponseVO(groupInfo);
    }

    private GroupInfo getGroupDetailCommon(HttpServletRequest request, String groupId){
        TokenUserInfoDto tokenUserInfoDto = getToken(request);
        UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(), groupId);
        if (null == userContact || !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())) {
            throw new BusinessException("你不在群聊或者群聊不存在或已解散");
        }
        GroupInfo groupInfo = this.groupInfoService.getGroupInfoByGroupId(groupId);
        if (null == groupInfo || !GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())) {
            throw new BusinessException("群聊不存在或已解散");
        }
        return groupInfo;
    }


    @RequestMapping("/getGroupInfo4Chat")
    //@GlobalInterceptor
    public ResponseVO getGroupInfo4Chat(HttpServletRequest request, @NotEmpty String groupId) {
        GroupInfo groupInfo = getGroupDetailCommon(request, groupId);

        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setQueryUserInfo(true);
        userContactQuery.setOrderBy("create_time ASC");
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> userContactList = this.userContactService.findListByParam(userContactQuery);

        GroupInfoVo groupInfoVo = new GroupInfoVo();
        groupInfoVo.setGroupInfo(groupInfo);
        groupInfoVo.setUserContactList(userContactList);

        return getSuccessResponseVO(groupInfoVo);
    }

    @RequestMapping("/addOrRemoveGroupUser")
    //@GlobalInterceptor
    public ResponseVO addOrRemoveGroupUser(HttpServletRequest request,
                                           @NotEmpty String groupId,
                                           @NotEmpty String selectContacts,
                                           @NotNull Integer opType) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);
        groupInfoService.addOrRemoveGroupUser(tokenUserInfoDto, groupId, selectContacts, opType);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/leaveGroup")
    //@GlobalInterceptor
    public ResponseVO leaveGroup(HttpServletRequest request, @NotEmpty String groupId) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);
        groupInfoService.leaveGroup(tokenUserInfoDto.getUserId(), groupId, MessageTypeEnum.LEAVE_GROUP);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/dissolutionGroup")
    //@GlobalInterceptor
    public ResponseVO dissolutionGroup(HttpServletRequest request, @NotEmpty String groupId) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);
        groupInfoService.dissolutionGroup(tokenUserInfoDto.getUserId(), groupId);
        return getSuccessResponseVO(null);
    }


}