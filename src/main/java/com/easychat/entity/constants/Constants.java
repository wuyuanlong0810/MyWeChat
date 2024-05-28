package com.easychat.entity.constants;

import com.easychat.entity.enums.UserContactTypeEnum;
import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-23 22:15
 */
public class Constants {

    public static final String REGEX_PASSWORD ="^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{0,18}$";
    public static final String REDIS_KEY_CHECK_CODE = "easychat:checkcode:";//存验证码
    public static final String REDIS_KEY_WS_USER_HEART_BEAT = "easychat:ws:user:heartbeat:";//存心跳
    public static final String REDIS_KEY_WS_TOKEN = "easychat:ws:token:";//存token
    public static final String REDIS_KEY_WS_TOKEN_USERID = "easychat:ws:token:userid:";//存userid

    public static final String ROBOT_UID = UserContactTypeEnum.USER.getPrefix() + "robot";//机器人编号
    public static final String REDIS_KEY_SYS_SETTING = "easychat:syssetting:";//默认系统配置

    public static final String FILE_FOLDER = "file/";
    public static final String AVATAR_FOLDER = "avatar/";
    public static final String IMAGE_SUFFIX = ".png";
    public static final String COVER_IMAGE = "_cover.png";
    public static final String APPLY_INFO_TEMPLATE = "我是%s";

    public static final String APP_UPDATE_FOLDER = "app/";

    public static final String APP_EXE_SUFFIX = ".exe";

}
