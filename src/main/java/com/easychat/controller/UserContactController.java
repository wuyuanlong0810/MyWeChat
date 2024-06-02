package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.dto.UserContactSearchResultDto;
import com.easychat.entity.enums.PageSize;
import com.easychat.entity.enums.ResponseCodeEnum;
import com.easychat.entity.enums.UserContactStatusEnum;
import com.easychat.entity.enums.UserContactTypeEnum;
import com.easychat.entity.po.UserContact;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.query.UserContactApplyQuery;
import com.easychat.entity.query.UserContactQuery;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.exception.BusinessException;
import com.easychat.service.UserContactApplyService;
import com.easychat.service.UserContactService;
import com.easychat.service.UserInfoService;
import com.sun.istack.internal.NotNull;
import jodd.util.ArraysUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-25 19:57
 */
@RestController
@RequestMapping("/contact")
public class UserContactController extends ABaseController {//用户好友控制层

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserContactApplyService userContactApplyService;

    @RequestMapping("/search")
    //@GlobalInterceptor
    public ResponseVO search(HttpServletRequest request, @NotEmpty String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);
        UserContactSearchResultDto userContactSearchResultDto = userContactService.searchContact(tokenUserInfoDto.getUserId(), contactId);
        return getSuccessResponseVO(userContactSearchResultDto);
    }

    @RequestMapping("/applyAdd")
    //@GlobalInterceptor
    public ResponseVO applyAdd(HttpServletRequest request, @NotEmpty String contactId, String applyInfo) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);
        Integer joinType = userContactService.applyAdd(tokenUserInfoDto, contactId, applyInfo);
        return getSuccessResponseVO(joinType);
    }

    @RequestMapping("/loadApply")
    //@GlobalInterceptor
    public ResponseVO loadApply(HttpServletRequest request, Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);

        UserContactApplyQuery applyQuery = new UserContactApplyQuery();
        applyQuery.setOrderBy("last_apply_time desc");
        applyQuery.setReceiveUserId(tokenUserInfoDto.getUserId());
        applyQuery.setPageNo(pageNo);
        applyQuery.setPageSize(PageSize.SIZE15.getSize());
        applyQuery.setQueryContactInfo(true);

        PaginationResultVO resultVo = userContactApplyService.findListByPage(applyQuery);
        return getSuccessResponseVO(resultVo);
    }

    @RequestMapping("/dealWithApply")
    //@GlobalInterceptor
    public ResponseVO dealWithApply(HttpServletRequest request, @NotNull Integer applyId, @NotNull Integer status) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);
        this.userContactApplyService.dealWithApply(tokenUserInfoDto.getUserId(), applyId, status);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadContact")
    //@GlobalInterceptor
    public ResponseVO loadContact(HttpServletRequest request, @NotNull String contactType) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByName(contactType);
        if (null == contactTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        TokenUserInfoDto tokenUserInfoDto = getToken(request);

        UserContactQuery contactQuery = new UserContactQuery();
        contactQuery.setUserId(tokenUserInfoDto.getUserId());
        contactQuery.setContactType(contactTypeEnum.getType());

        if (UserContactTypeEnum.USER == contactTypeEnum) {
            contactQuery.setQueryContactUserInfo(true);
        } else if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            contactQuery.setQueryGroupInfo(true);
            contactQuery.setExcludeMyGroup(true);
        }

        contactQuery.setOrderBy("last_update_time desc");
        contactQuery.setStatusArray(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus()
        });
        List<UserContact> userContacts = userContactService.findListByParam(contactQuery);

        return getSuccessResponseVO(userContacts);
    }

    /**
     * 获取联系人信息，不一定是好友
     *
     * @param request
     * @param contactId
     * @return
     */
    @RequestMapping("/getContactInfo")
    //@GlobalInterceptor
    public ResponseVO getContactInfo(HttpServletRequest request, @NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);

        UserInfo userInfo = userInfoService.getUserInfoByUserId(contactId);

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(userInfo, userInfoVO);
        userInfoVO.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());

        UserContact userContact = userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(), contactId);
        if (userContact != null) {
            userInfoVO.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
        }

        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * 获取联系人信息，必须是好友
     *
     * @param request
     * @param contactId
     * @return
     */
    @RequestMapping("/getContactUserInfo")
    //@GlobalInterceptor
    public ResponseVO getContactUserInfo(HttpServletRequest request, @NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);

        UserContact userContact = userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(), contactId);

        if (null == userContact || !ArraysUtil.contains(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus()
        }, userContact.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        UserInfo userInfo = userInfoService.getUserInfoByUserId(contactId);

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(userInfo, userInfoVO);

        return getSuccessResponseVO(userInfoVO);
    }

    @RequestMapping("/delContact")
    //@GlobalInterceptor
    public ResponseVO delContact(HttpServletRequest request, @NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);
        userContactService.removeUserContact(tokenUserInfoDto.getUserId(), contactId, UserContactStatusEnum.DEL);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/addContact2BlackList")
    //@GlobalInterceptor
    public ResponseVO addContact2BlackList(HttpServletRequest request, @NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);
        userContactService.removeUserContact(tokenUserInfoDto.getUserId(), contactId, UserContactStatusEnum.BLACKLIST);
        return getSuccessResponseVO(null);
    }

}

