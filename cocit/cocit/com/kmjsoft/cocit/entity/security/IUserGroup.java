package com.kmjsoft.cocit.entity.security;

import java.util.Date;

/**
 * 用于描述用户属于哪些组或组包含了哪些用户成员？
 * 
 * @author Ji Yongshan
 * 
 */
public interface IUserGroup {
	
	String getUserGuid();

	String getGroupGuid();

	Date getExpiredFrom();

	Date getExpiredTo();

}
