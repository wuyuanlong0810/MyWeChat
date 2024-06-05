package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.service.UserInfoService;
import com.easychat.utils.StringTools;
import com.easychat.websocket.ChannelContextUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.IOException;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-28 16:08
 */
@RestController
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController{//用户信息控制层
    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @RequestMapping("/getUserInfo")
    //@GlobalInterceptor
    public ResponseVO getUserInfo(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);
        UserInfo userInfo = userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
        UserInfoVO userInfoVo = new UserInfoVO();
        BeanUtils.copyProperties(userInfo,userInfoVo);
        userInfoVo.setAdmin(tokenUserInfoDto.getAdmin());
        return getSuccessResponseVO(userInfoVo);
    }

    @RequestMapping("/saveUserInfo")
    //@GlobalInterceptor
    public ResponseVO saveUserInfo(HttpServletRequest request, UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfo.setPassword(null);
        userInfo.setStatus(null);
        userInfo.setCreateTime(null);
        userInfo.setLastLoginTime(null);

        this.userInfoService.updateUserInfo(userInfo, avatarFile, avatarCover);
        return getUserInfo(request);
    }

    @RequestMapping("/updatePassword")
    //@GlobalInterceptor
    public ResponseVO updatePassword(HttpServletRequest request,
                                     @NotEmpty String password) {//@Pattern(regexp = Constants.REGEX_PASSWORD)
        TokenUserInfoDto tokenUserInfoDto = getToken(request);

        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(StringTools.encodeMD5(password));

        this.userInfoService.updateUserInfoByUserId(userInfo, tokenUserInfoDto.getUserId());

        //下线
        channelContextUtils.closeContext(userInfo.getUserId());

        return getSuccessResponseVO(null);
    }

    @RequestMapping("/logout")
    //@GlobalInterceptor
    public ResponseVO logout(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getToken(request);

        //下线
        channelContextUtils.closeContext(tokenUserInfoDto.getUserId());

        return getSuccessResponseVO(null);
    }


}
