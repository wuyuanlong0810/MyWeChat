package com.mywechat.service;

import java.util.Iterator;
import java.util.List;

import com.mywechat.entity.dto.TokenUserInfoDto;
import com.mywechat.entity.enums.MessageTypeEnum;
import com.mywechat.entity.query.GroupInfoQuery;
import com.mywechat.entity.po.GroupInfo;
import com.mywechat.entity.vo.PaginationResultVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 *  业务接口
 */
public interface GroupInfoService {

	/**
	 * 根据条件查询列表
	 */
	List<GroupInfo> findListByParam(GroupInfoQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(GroupInfoQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery param);

	/**
	 * 新增
	 */
	Integer add(GroupInfo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<GroupInfo> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<GroupInfo> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(GroupInfo bean,GroupInfoQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(GroupInfoQuery param);

	/**
	 * 根据GroupId查询对象
	 */
	GroupInfo getGroupInfoByGroupId(String groupId);


	/**
	 * 根据GroupId修改
	 */
	Integer updateGroupInfoByGroupId(GroupInfo bean,String groupId);


	/**
	 * 根据GroupId删除
	 */
	Integer deleteGroupInfoByGroupId(String groupId);

	/**
	 * 创建群组
	 * @param groupInfo
	 * @param avatarFile
	 * @param avatarCover
	 */
	void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover);

	void dissolutionGroup(String groupOwnerId,String groupId);

	void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String contactIds, Integer opType);

	void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum);
}