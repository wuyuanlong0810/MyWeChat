package com.easychat.entity.enums;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-24 16:54
 */
public enum BeautyAccountStatusEnum {
    UNUSED(0,"未使用"),
    USED(1,"已使用");
    private Integer status;
    private String desc;

    BeautyAccountStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static BeautyAccountStatusEnum getByStatus(Integer status){
        for (BeautyAccountStatusEnum value : BeautyAccountStatusEnum.values()) {
            if(value.getStatus().equals(status)){
                return value;
            }
        }
        return null;
    }
}
