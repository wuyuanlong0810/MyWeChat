package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.po.AppUpdate;
import com.easychat.entity.query.AppUpdateQuery;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.service.AppUpdateService;
import com.sun.istack.internal.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-28 22:20
 */
@RestController
@RequestMapping("/admin")
public class AdminAppUpdateController extends ABaseController {

    @Resource
    private AppUpdateService appUpdateService;


    @RequestMapping("/loadUpdateList")
    //@GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUpdateList(AppUpdateQuery query) {
        query.setOrderBy("id desc");
        return getSuccessResponseVO(appUpdateService.findListByPage(query));
    }

    @RequestMapping("/saveUpdate")
    //@GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveUpdate(Integer id,
                                 @NotEmpty String version,
                                 @NotEmpty String updateDesc,
                                 @NotNull Integer fileType,
                                 String outerLink,
                                 MultipartFile file) throws IOException {

        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setId(id);
        appUpdate.setVersion(version);
        appUpdate.setUpdateDesc(updateDesc);
        appUpdate.setFileType(fileType);
        appUpdate.setOuterLink(outerLink);

        appUpdateService.saveUpdate(appUpdate, file);
        return getSuccessResponseVO(null);
    }
}
