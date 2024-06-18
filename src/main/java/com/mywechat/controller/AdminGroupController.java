package com.mywechat.controller;

import com.mywechat.annotation.GlobalInterceptor;
import com.mywechat.entity.enums.ResponseCodeEnum;
import com.mywechat.entity.po.GroupInfo;
import com.mywechat.entity.query.GroupInfoQuery;
import com.mywechat.entity.vo.PaginationResultVO;
import com.mywechat.entity.vo.ResponseVO;
import com.mywechat.exception.BusinessException;
import com.mywechat.service.GroupInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-28 20:50
 */
@RestController
@RequestMapping("/admin")
public class AdminGroupController extends ABaseController{//管理群组控制层

    @Resource
    private GroupInfoService groupInfoService;

    @RequestMapping("/loadGroup")
    //@GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadGroup(GroupInfoQuery query) {
        query.setOrderBy("create_time desc");
        query.setQueryMemberCount(true);
        query.setQueryGroupOwnerName(true);
        PaginationResultVO resultVo = groupInfoService.findListByPage(query);
        return getSuccessResponseVO(resultVo);
    }

    @RequestMapping("/dissolutionGroup")
    //@GlobalInterceptor(checkAdmin = true)
    public ResponseVO dissolutionGroup(@NotEmpty String groupId) {
        GroupInfo groupInfo = groupInfoService.getGroupInfoByGroupId(groupId);
        if (groupInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        groupInfoService.dissolutionGroup(groupInfo.getGroupOwnerId(), groupId);
        return getSuccessResponseVO(null);
    }



}
