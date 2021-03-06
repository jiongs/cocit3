package com.kmjsoft.cocit.entity;

/**
 * “树形结构实体”接口：实现该接口的所有实体类均为树形实体类，实体对象集是一棵自身递归树。
 * 
 * @author Ji Yongshan
 * 
 * @param <T>
 */
public interface ITreeEntity extends INamedEntity {

	/**
	 * 父亲GUID：逻辑外键，关联到 {@link #getDataGuid()}
	 * 
	 * @return
	 */
	public String getParentGuid();

	public void setParentGuid(String parentGuid);

	/**
	 * 冗余字段：租户名称
	 * 
	 * @return
	 */
	String getParentName();

	void setParentName(String parentName);
}
