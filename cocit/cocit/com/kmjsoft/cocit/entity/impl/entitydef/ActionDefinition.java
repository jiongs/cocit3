package com.kmjsoft.cocit.entity.impl.entitydef;

import static com.kmjsoft.cocit.entity.EntityConst.BIZCATA_DEMSY_ADMIN;
import static com.kmjsoft.cocit.entity.EntityConst.BIZSYS_DEMSY_LIB_ACTION;
import static com.kmjsoft.cocit.entity.EntityConst.ORDER_DEMSY_LIB_ACTION;
import static com.kmjsoft.cocit.entityengine.manager.BizConst.TYPE_BZFORM_NEW;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.kmjsoft.cocit.entity.impl.security.BaseAction;
import com.kmjsoft.cocit.entity.security.IAction;
import com.kmjsoft.cocit.orm.annotation.CocField;
import com.kmjsoft.cocit.orm.annotation.CocGroup;
import com.kmjsoft.cocit.orm.annotation.CocOperation;
import com.kmjsoft.cocit.orm.annotation.CocTable;

@Entity
@CocTable(name = "业务操作组件库", code = BIZSYS_DEMSY_LIB_ACTION, catalog = BIZCATA_DEMSY_ADMIN, orderby = ORDER_DEMSY_LIB_ACTION, buildin = true//
, actions = { @CocOperation(name = "新增操作", typeCode = TYPE_BZFORM_NEW, mode = "c")//
		, @CocOperation(jsonData = "CommonBizAction.data.js") //
}//
, groups = { @CocGroup(name = "基本信息", code = "basic"//
, fields = { @CocField(name = "操作名称", property = "name", mode = "c:M e:M")//
		, @CocField(name = "操作编号", property = "code") //
		, @CocField(name = "人工顺序", property = "orderby") //
		, @CocField(name = "模式编码", property = "mode", desc = "用于识别执行该操作时各个字段的显示模式") //
		, @CocField(name = "类型代码", property = "typeCode", desc = "该业务代码用于识别操作类型") //
		, @CocField(name = "业务插件", property = "plugin", desc = "执行操作时将同时调用业务插件中的方法") //
		, @CocField(name = "分类操作", property = "parentAction", fkTable = BIZSYS_DEMSY_LIB_ACTION) //
		, @CocField(name = "分类操作", property = "parentAction.name") //
		, @CocField(name = "停用状态", property = "disabled", options = "1:停用,0:启用") //
		, @CocField(name = "按钮徽标", property = "logo", uploadType = "*.jpg;*.gif;*.png") //
		, @CocField(name = "按钮图片", property = "image", uploadType = "*.jpg;*.gif;*.png") //
		, @CocField(name = "窗体模版", property = "template", desc = "用于展现业务操作界面") //
		, @CocField(name = "成功提示", property = "info", desc = "执行业务操作成功后，弹出这里设置的提示信息") //
		, @CocField(name = "警告提示", property = "warn", desc = "执行业务操作时，遇到警告则提示该信息") //
		, @CocField(name = "错误提示", property = "error", desc = "执行业务操作出错时，弹出这里设置的提示信息") //
		, @CocField(name = "操作描述", property = "desc") //
		, @CocField(name = "创建时间", property = "created", mode = "*:P") //
		, @CocField(name = "更新时间", property = "updated", mode = "*:P") //
		, @CocField(name = "创建帐号", property = "createdBy", mode = "*:P") //
		, @CocField(name = "更新帐号", property = "updatedBy", mode = "*:P") //
}) }// end groups
, jsonData = "ActionDefinition.data.js"//
)
public class ActionDefinition extends BaseAction implements IAction {
	@ManyToOne
	private ActionDefinition parentAction;

	@OneToMany(mappedBy = "parentAction")
	protected List<ActionDefinition> children;

	public ActionDefinition getParentAction() {
		return parentAction;
	}

	public void setParentAction(ActionDefinition parent) {
		this.parentAction = parent;
	}

	public List<ActionDefinition> getChildren() {
		return children;
	}

	public void setChildren(List<ActionDefinition> children) {
		this.children = children;
	}

	@Override
	public IAction getActionDefinition() {
		return null;
	}

}