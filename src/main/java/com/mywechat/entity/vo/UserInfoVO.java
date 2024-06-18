package com.mywechat.entity.vo;

import com.mywechat.entity.enums.DateTimePatternEnum;
import com.mywechat.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户信息
 */
public class UserInfoVO implements Serializable {


	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 昵称
	 */
	private String nickName;

	/**
	 * 0：直接加入 1：同意后加好友
	 */
	private Integer joinType;

	/**
	 * 性别
	 */
	private Integer sex;


	/**
	 * 个人签名
	 */
	private String personSignature;

	/**
	 * 状态
	 */
	private Integer contactStatus;

	/**
	 * 地区
	 */
	private String areaName;

	/**
	 * 地区编号
	 */
	private String areaCode;


	private String token;
	private Boolean admin;

	public String getUserId() {
		return userId;
	}

	@Override
	public String toString() {
		return "UserInfoVO{" +
				"userId='" + userId + '\'' +
				", nickName='" + nickName + '\'' +
				", joinType=" + joinType +
				", sex=" + sex +
				", personSignature='" + personSignature + '\'' +
				", contactStatus=" + contactStatus +
				", areaName='" + areaName + '\'' +
				", areaCode='" + areaCode + '\'' +
				", token='" + token + '\'' +
				", admin=" + admin +
				'}';
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public Integer getJoinType() {
		return joinType;
	}

	public void setJoinType(Integer joinType) {
		this.joinType = joinType;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public String getPersonSignature() {
		return personSignature;
	}

	public void setPersonSignature(String personSignature) {
		this.personSignature = personSignature;
	}

	public Integer getContactStatus() {
		return contactStatus;
	}

	public void setContactStatus(Integer contactStatus) {
		this.contactStatus = contactStatus;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}
}
