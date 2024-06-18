package com.mywechat.controller;

import com.mywechat.annotation.GlobalInterceptor;
import com.mywechat.entity.query.UserInfoQuery;
import com.mywechat.entity.vo.PaginationResultVO;
import com.mywechat.entity.vo.ResponseVO;
import com.mywechat.service.UserInfoService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-28 20:03
 */
@RestController
@RequestMapping("/admin")
public class AdminUserInfoController extends ABaseController{//管理用户信息控制层
    @Resource
    private UserInfoService userInfoService;


    @RequestMapping("/loadUser")
    //@GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUser(UserInfoQuery userInfoQuery) {
        userInfoQuery.setOrderBy("create_time desc");
        PaginationResultVO resultVo = userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(resultVo);
    }

    @RequestMapping("/updateUserStatus")
    //@GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserStatus(@NotNull Integer status, @NotEmpty String userId) {
        userInfoService.updateUserStatus(status, userId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/forceOffLine")
    //@GlobalInterceptor(checkAdmin = true)
    public ResponseVO forceOffline(@NotEmpty String userId) {
        userInfoService.forceOffLine(userId);
        return getSuccessResponseVO(null);
    }

}
