package com.mywechat.controller;

import com.mywechat.annotation.GlobalInterceptor;
import com.mywechat.entity.po.AppUpdate;
import com.mywechat.entity.query.AppUpdateQuery;
import com.mywechat.entity.vo.ResponseVO;
import com.mywechat.service.AppUpdateService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-28 22:20
 */
@RestController
@RequestMapping("/admin")
public class AdminAppUpdateController extends ABaseController {//管理版本控制层

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

    @RequestMapping("/delUpdate")
//    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO delUpdate(@NotNull Integer id) {
        appUpdateService.deleteAppUpdateById(id);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/postUpdate")
//    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO postUpdate(@NotNull Integer id, @NotNull Integer status, String grayscaleUid) {
        appUpdateService.postUpdate(id, status, grayscaleUid);
        return getSuccessResponseVO(null);
    }



}
