package com.mywechat.controller;

import com.mywechat.annotation.GlobalInterceptor;
import com.mywechat.entity.config.Appconfig;
import com.mywechat.entity.constants.Constants;
import com.mywechat.entity.dto.TokenUserInfoDto;
import com.mywechat.entity.enums.AppUpdateFileTypeEnum;
import com.mywechat.entity.po.AppUpdate;
import com.mywechat.entity.vo.AppUpdateVO;
import com.mywechat.entity.vo.ResponseVO;
import com.mywechat.service.AppUpdateService;
import com.mywechat.utils.StringTools;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-29 17:20
 */
@RestController
@RequestMapping("/update")
public class UpdateController extends ABaseController{//版本更新控制层

    @Resource
    private AppUpdateService appUpdateService;

    @Resource
    private Appconfig appconfig;

    @RequestMapping("/checkVersion")
    //@GlobalInterceptor
    public ResponseVO checkVersion(HttpServletRequest request,String appVersion){
        TokenUserInfoDto token = getToken(request);

        String uid = token.getUserId();

        if (StringTools.isEmpty(appVersion)){
            return getSuccessResponseVO(null);
        }
        AppUpdate appUpdate = appUpdateService.getLatestUpdate(appVersion,uid);
        if (appUpdate == null){
            return getSuccessResponseVO(null);
        }
        AppUpdateVO updateVO = new AppUpdateVO();
        BeanUtils.copyProperties(appUpdate,updateVO);
        if (AppUpdateFileTypeEnum.LOCAL.getType().equals(appUpdate.getFileType())) {
            File file = new File(appconfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER + appUpdate.getId() + Constants.APP_EXE_SUFFIX);
            updateVO.setSize(file.length());
        } else {
            updateVO.setSize(0L);
        }

        // Set update description array
        updateVO.setUpdateList(Arrays.asList(appUpdate.getUpdateDescArray()));

        // Set the file name
        String fileName = Constants.APP_NAME + appUpdate.getVersion() + Constants.APP_EXE_SUFFIX;
        updateVO.setFileName(fileName);


        return getSuccessResponseVO(updateVO);
    }



}
