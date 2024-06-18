package com.mywechat.entity.vo;

import com.mywechat.entity.po.GroupInfo;
import com.mywechat.entity.po.UserContact;

import java.util.List;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-25 19:19
 */
public class GroupInfoVo {
    private GroupInfo groupInfo;
    private List<UserContact> userContactList;

    public List<UserContact> getUserContactList() {
        return userContactList;
    }

    public void setUserContactList(List<UserContact> userContactList) {
        this.userContactList = userContactList;
    }

    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }
}
