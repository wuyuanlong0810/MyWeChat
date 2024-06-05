package com.easychat.entity.enums;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-24 16:54
 */
public enum AddOrRemoveTypeEnum {
    REMOVE(0,"离开群"),
    ADD(1,"加入群");
    private Integer type;
    private String desc;

    AddOrRemoveTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static AddOrRemoveTypeEnum getByType(Integer type){
        for (AddOrRemoveTypeEnum value : AddOrRemoveTypeEnum.values()) {
            if(value.getType().equals(type)){
                return value;
            }
        }
        return null;
    }
}
