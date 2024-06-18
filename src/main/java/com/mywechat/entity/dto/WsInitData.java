package com.mywechat.entity.dto;

import com.mywechat.entity.po.ChatMessage;
import com.mywechat.entity.po.ChatSessionUser;

import java.util.List;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-30 21:11
 */
public class WsInitData {

    private List<ChatSessionUser> chatSessionList;
    private List<ChatMessage> chatMessageList;
    private Integer applyCount;

    public List<ChatSessionUser> getChatSessionList() {
        return chatSessionList;
    }

    public void setChatSessionList(List<ChatSessionUser> chatSessionList) {
        this.chatSessionList = chatSessionList;
    }

    public List<ChatMessage> getChatMessageList() {
        return chatMessageList;
    }

    public void setChatMessageList(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    public Integer getApplyCount() {
        return applyCount;
    }

    public void setApplyCount(Integer applyCount) {
        this.applyCount = applyCount;
    }
}
