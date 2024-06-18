package com.mywechat.controller;

import com.mywechat.annotation.GlobalInterceptor;
import com.mywechat.entity.po.UserInfoBeauty;
import com.mywechat.entity.query.UserInfoBeautyQuery;
import com.mywechat.entity.vo.PaginationResultVO;
import com.mywechat.entity.vo.ResponseVO;
import com.mywechat.service.UserInfoBeautyService;
import javax.validation.constraints.NotNull;
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
