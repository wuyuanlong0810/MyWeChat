package com.easychat.entity.enums;

import com.easychat.utils.StringTools;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-25 16:28
 */
public enum UserContactStatusEnum {
    NOT_FRIEND(0, "非好友"),
    FRIEND(1, "好友"),
    DEL(2, "已删除好友"),
    DEL_BE(3, "被好友删除"),
    BLACKLIST(4, "已拉黑好友"),
    BLACKLIST_BE(5, "被好友拉黑"),
    BLACKLIST_BE_FIRST(6, "首次被好友拉黑");

    private final Integer status;
    private final String desc;

    UserContactStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static UserContactStatusEnum getByStatus(Integer status) {
        for (UserContactStatusEnum value : UserContactStatusEnum.values()) {
            if(value.getStatus().equals(status)){
                return value;
            }
        }
        return null;
    }
}

