package com.mywechat.entity.enums;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-24 16:54
 */
public enum JoinTypeEnum {
    JOIN(0,"直接加入"),
    APPLY(1,"需要审核");
    private Integer type;
    private String desc;

    JoinTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static JoinTypeEnum getByType(Integer type){
        for (JoinTypeEnum value : JoinTypeEnum.values()) {
            if(value.getType().equals(type)){
                return value;
            }
        }
        return null;
    }
}
