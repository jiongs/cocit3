package com.kmjsoft.cocit.entity.module;

import com.kmjsoft.cocit.entity.INamedEntity;

/**
 * 字段分组
 * 
 * @author yongshan.ji
 */
public interface IEntityColumnGroup extends INamedEntity {
	String getModuleGuid();

	void setModuleGuid(String moduleGuid);

	/**
	 * <b>字段显示模式(mode)：</b>用空格分隔，与子系统数据操作中指定的动作模式组合使用。
	 * <ul>
	 * <li>M: Must 必需的</li>
	 * <li>E: Edit 可编辑的 (即可读写)</li>
	 * <li>I: Inspect 检查（带有一个隐藏字段存放其值）</li>
	 * <li>S: Show 显示（但不带隐藏字段）</li>
	 * <li>N: None 不显示</li>
	 * <li>P: Present 如果该字段有值就显示，否则如果没有值就不显示该字段</li>
	 * <li>H: Hidden 隐藏 (不显示，但有一个隐藏框存在)</li>
	 * <li>R: Read only 只读</li>
	 * <li>D: Disable 禁用</li>
	 * </ul>
	 * <p>
	 * <b> 字段显示模式举例说明(mode)： </b>
	 * <ul>
	 * <li>v:I——查看数据时，该字段处于检查模式</li>
	 * <li>e:E——编辑数据时，字段可编辑</li>
	 * <li>bu:N——批量修改数据时，字段不可见</li>
	 * </ul>
	 */
	String getMode();

	void setMode(String mode);
}
