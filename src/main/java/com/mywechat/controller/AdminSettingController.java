package com.mywechat.controller;

import com.mywechat.annotation.GlobalInterceptor;
import com.mywechat.entity.config.Appconfig;
import com.mywechat.entity.constants.Constants;
import com.mywechat.entity.dto.SysSettingDto;
import com.mywechat.entity.vo.ResponseVO;
import com.mywechat.redis.RedisComponent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-28 21:51
 */
@RestController
@RequestMapping("/admin")
public class AdminSettingController extends ABaseController{//管理系统设置控制层

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private Appconfig appconfig;

    @RequestMapping("/getSysSetting")
    //@GlobalInterceptor(checkAdmin = true)
    public ResponseVO getSysSetting() {
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        return getSuccessResponseVO(sysSettingDto);
    }

    @RequestMapping("/saveSysSetting")
    //@GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveSysSetting(SysSettingDto sysSettingDto,
                                     MultipartFile robotFile,
                                     MultipartFile robotCover) throws IOException {
        if (robotFile != null) {
            String baseFolder = appconfig.getProjectFolder() + Constants.FILE_FOLDER;
            File targetFileFolder = new File(baseFolder + Constants.AVATAR_FOLDER);
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + Constants.ROBOT_UID + Constants.IMAGE_SUFFIX;
            robotFile.transferTo(new File(filePath));
            robotCover.transferTo(new File(filePath + Constants.COVER_IMAGE));
        }
        redisComponent.saveSysSetting(sysSettingDto);
        return getSuccessResponseVO(null);
    }



}
