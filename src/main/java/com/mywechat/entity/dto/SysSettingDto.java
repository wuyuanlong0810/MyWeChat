package com.mywechat.entity.dto;

import com.mywechat.entity.constants.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-24 21:46
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingDto implements Serializable {

    private Integer maxGroupCount = 5;//最大群组数
    private Integer maxGroupMemberCount = 500;//群最多人数
    private Integer maxImageSize = 2;//图片大小MB
    private Integer maxVideoSize = 5;//视频大小MB
    private Integer maxFileSize = 5;//文件大小MB
    private String robotUid = Constants.ROBOT_UID;//机器人ID
    private String robotNickName = "MyWeChat";//机器人昵称
    private String robotWelcome = "欢迎使用MyWeChat";//欢迎消息

    public Integer getMaxGroupCount() {
        return maxGroupCount;
    }

    public void setMaxGroupCount(Integer maxGroupCount) {
        this.maxGroupCount = maxGroupCount;
    }

    public Integer getMaxGroupMemberCount() {
        return maxGroupMemberCount;
    }

    public void setMaxGroupMemberCount(Integer maxGroupMemberCount) {
        this.maxGroupMemberCount = maxGroupMemberCount;
    }

    public Integer getMaxImageSize() {
        return maxImageSize;
    }

    public void setMaxImageSize(Integer maxImageSize) {
        this.maxImageSize = maxImageSize;
    }

    public Integer getMaxVideoSize() {
        return maxVideoSize;
    }

    public void setMaxVideoSize(Integer maxVideoSize) {
        this.maxVideoSize = maxVideoSize;
    }

    public Integer getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Integer maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String getRobotUid() {
        return robotUid;
    }

    public void setRobotUid(String robotUid) {
        this.robotUid = robotUid;
    }

    public String getRobotNickName() {
        return robotNickName;
    }

    public void setRobotNickName(String robotNickName) {
        this.robotNickName = robotNickName;
    }

    public String getRobotWelcome() {
        return robotWelcome;
    }

    public void setRobotWelcome(String robotWelcome) {
        this.robotWelcome = robotWelcome;
    }
}
