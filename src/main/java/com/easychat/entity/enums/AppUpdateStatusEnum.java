package com.easychat.entity.enums;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-28 22:36
 */
public enum AppUpdateStatusEnum {
    INIT(0, "未发布"),
    GRAYSCALE(1, "灰度发布"),
    ALL(2, "全网发布");

    private Integer status;
    private String description;

    AppUpdateStatusEnum(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public static AppUpdateStatusEnum getByStatus(Integer status) {
        for (AppUpdateStatusEnum enumItem : AppUpdateStatusEnum.values()) {
            if (enumItem.status.equals(status)) {
                return enumItem;
            }
        }
        return null;
    }
}

