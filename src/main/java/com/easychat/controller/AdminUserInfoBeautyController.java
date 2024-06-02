package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.po.UserInfoBeauty;
import com.easychat.entity.query.UserInfoBeautyQuery;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.service.UserInfoBeautyService;
import com.sun.istack.internal.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-28 20:15
 */
@RestController
@RequestMapping("/admin")
public class AdminUserInfoBeautyController extends ABaseController{//管理靓号控制层

    @Resource
    private UserInfoBeautyService userInfoBeautyService;

    @RequestMapping("/loadBeautyAccountList")
    //@GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadBeautyAccountList(UserInfoBeautyQuery query) {
        query.setOrderBy("id desc");
        PaginationResultVO resultVo = userInfoBeautyService.findListByPage(query);
        return getSuccessResponseVO(resultVo);
    }

    @RequestMapping("/saveBeautAccount")
    //@GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveBeautAccount(UserInfoBeauty beauty) {
        userInfoBeautyService.saveAccount(beauty);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delBeautAccount")
    //@GlobalInterceptor(checkAdmin = true)
    public ResponseVO delBeautAccount(@NotNull Integer id) {
        userInfoBeautyService.deleteUserInfoBeautyById(id);
        return getSuccessResponseVO(null);
    }

}
