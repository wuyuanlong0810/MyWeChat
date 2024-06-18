package com.mywechat.service;

import java.io.File;
import java.util.List;

import com.mywechat.entity.dto.MessageSendDto;
import com.mywechat.entity.dto.TokenUserInfoDto;
import com.mywechat.entity.query.ChatMessageQuery;
import com.mywechat.entity.po.ChatMessage;
import com.mywechat.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;


/**
 * 聊天消息表 业务接口
 */
public interface ChatMessageService {

	/**
	 * 根据条件查询列表
	 */
	List<ChatMessage> findListByParam(ChatMessageQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(ChatMessageQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery param);

	/**
	 * 新增
	 */
	Integer add(ChatMessage bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ChatMessage> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ChatMessage> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(ChatMessage bean,ChatMessageQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(ChatMessageQuery param);

	/**
	 * 根据MessageId查询对象
	 */
	ChatMessage getChatMessageByMessageId(Long messageId);


	/**
	 * 根据MessageId修改
	 */
	Integer updateChatMessageByMessageId(ChatMessage bean,Long messageId);


	/**
	 * 根据MessageId删除
	 */
	Integer deleteChatMessageByMessageId(Long messageId);

	MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto);

	void saveMessageFile(String userId, Long messageId, MultipartFile file,MultipartFile cover);

	File downloadFile(TokenUserInfoDto token, Long messageId, Boolean showCover);
}