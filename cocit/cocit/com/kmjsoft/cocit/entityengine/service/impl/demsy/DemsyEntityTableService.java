package com.kmjsoft.cocit.entityengine.service.impl.demsy;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.jiongsoft.cocit.lang.Obj;
import com.jiongsoft.cocit.lang.SystemExcel;
import com.kmjsoft.cocit.Demsy;
import com.kmjsoft.cocit.entity.impl.module.EntityAction;
import com.kmjsoft.cocit.entity.impl.module.EntityColumn;
import com.kmjsoft.cocit.entity.impl.module.EntityColumnGroup;
import com.kmjsoft.cocit.entity.impl.module.EntityModule;
import com.kmjsoft.cocit.entity.module.IEntityColumn;
import com.kmjsoft.cocit.entity.module.IEntityModule;
import com.kmjsoft.cocit.entityengine.module.impl.BizEngine;
import com.kmjsoft.cocit.entityengine.service.FieldGroupService;
import com.kmjsoft.cocit.entityengine.service.FieldService;
import com.kmjsoft.cocit.entityengine.service.OperationService;
import com.kmjsoft.cocit.entityengine.service.TableService;
import com.kmjsoft.cocit.orm.ExtOrm;
import com.kmjsoft.cocit.orm.expr.CndExpr;
import com.kmjsoft.cocit.util.ClassUtil;
import com.kmjsoft.cocit.util.CocException;
import com.kmjsoft.cocit.util.KeyValue;
import com.kmjsoft.cocit.util.ObjectUtil;
import com.kmjsoft.cocit.util.SortUtil;
import com.kmjsoft.cocit.util.StringUtil;
import com.kmjsoft.cocit.util.Tree;
import com.kmjsoft.cocit.util.Tree.Node;

public class DemsyEntityTableService implements TableService {
	private BizEngine bizEngine;

	private EntityModule entity;

	private List<FieldGroupService> bizGroups;

	private List<FieldService> bizFields;

	private List<OperationService> bizOperations;

	private Properties extProps;

	DemsyEntityTableService(EntityModule e) {
		bizEngine = (BizEngine) Demsy.entityModuleManager;

		entity = e;
		bizGroups = new ArrayList();
		bizFields = new ArrayList();
		bizOperations = new ArrayList();
		extProps = new Properties();

		List<EntityColumn> dataFields = (List<EntityColumn>) bizEngine.getFields(entity);
		List<EntityAction> dataOperations = (List<EntityAction>) bizEngine.getActions(entity);

		initDataFields(dataFields);
		initDataOperations(dataOperations);
	}

	// @Override
	// public Properties getExtProps() {
	// return entity.getProperties();
	// }

	@Override
	public Long getID() {
		return entity.getId();
	}

	@Override
	public String getName() {
		return entity.getName();
	}

	@Override
	public boolean isDisabled() {
		return entity.isDisabled();
	}

	@Override
	public Date getOperatedDate() {
		return entity.getUpdatedDate();
	}

	@Override
	public String getOperatedUser() {
		return entity.getUpdatedUser();
	}

	// @Override
	// public <T> T get(String propName, T defaultReturn) {
	// String value = this.extProps.getProperty(propName);
	// if (value == null)
	// value = entity.get(propName);
	//
	// if (value == null)
	// return defaultReturn;
	// if (defaultReturn == null)
	// return (T) value;
	//
	// Class valueType = defaultReturn.getClass();
	//
	// try {
	// return (T) StringUtil.castTo(value, valueType);
	// } catch (Throwable e) {
	// Log.warn("", e);
	// }
	//
	// return defaultReturn;
	// }

	@Override
	public List<FieldGroupService> getEntityGroups() {
		return bizGroups;
	}

	@Override
	public List<FieldService> getEntityFields() {
		return bizFields;
	}

	@Override
	public List<OperationService> getEntityOperations() {
		return bizOperations;
	}

	@Override
	public List<FieldService> getEntityFieldsForNaviTree() {
		List<FieldService> ret = new ArrayList();

		for (FieldService f : this.bizFields) {
			DemsyEntityFieldService field = (DemsyEntityFieldService) f;
			EntityColumn data = field.getEntity();

			if (data.isDisabledNavi() || data.isDisabled()) {
				continue;
			}

			if ((field.isFK() && !data.isMappingToMaster()) || field.isV1Dic() || field.isBoolean() || !StringUtil.isNil(data.getOptions()))
				ret.add(field);

		}

		return ret;
	}

	public List<FieldService> getEntityFieldsForSelfTree() {
		List<FieldService> ret = new ArrayList();

		for (FieldService f : this.bizFields) {
			DemsyEntityFieldService field = (DemsyEntityFieldService) f;
			EntityColumn data = field.getEntity();

			if (data.isDisabledNavi() || data.isDisabled()) {
				continue;
			}

			if (field.isFK() && data.getRefrenceSystem().equals(this.entity)) {
				ret.add(field);
				break;
			}

		}

		return ret;
	}

	@Override
	public List<FieldService> getEntityFieldsForGrid() {
		List ret = new ArrayList();

		for (FieldService f : this.bizFields) {
			DemsyEntityFieldService field = (DemsyEntityFieldService) f;
			EntityColumn data = field.getEntity();

			if (data.isDisabledNavi() && data.isDisabled())
				continue;

			if (data.getRefrenceSystem() != null && data.getRefrenceField() != null)
				continue;

			if (data.isGridField() && data.getGridOrder() > 0)
				ret.add(field);

		}

		SortUtil.sort(ret, "gridOrder", true);

		return ret;
	}

	private void initDataFields(List<EntityColumn> dataFields) {
		if (dataFields == null)
			return;
		Map<Long, DemsyEntityGroupService> bizGroupsMap = new HashMap();

		for (EntityColumn systemData : dataFields) {
			EntityColumnGroup dataGroup = systemData.getDataGroup();
			Long groupID = dataGroup.getId();

			DemsyEntityGroupService bizGroup = (DemsyEntityGroupService) bizGroupsMap.get(groupID);
			if (bizGroup == null) {
				bizGroup = new DemsyEntityGroupService(dataGroup);
				bizGroupsMap.put(groupID, bizGroup);
				bizGroups.add(bizGroup);
			}

			FieldService bizField = new DemsyEntityFieldService(systemData);
			this.bizFields.add(bizField);
			bizGroup.addField(bizField);
		}
	}

	public Map<String, FieldService> getBizFieldsMapByPropName() {
		Map<String, FieldService> map = new HashMap();
		for (FieldService f : this.bizFields) {
			map.put(f.getPropName(), f);
		}

		return map;
	}

	private void initDataOperations(List<EntityAction> dataOperations) {
		if (dataOperations == null)
			return;

		for (EntityAction g : dataOperations) {
			this.bizOperations.add(new DemsyEntityOperationService(g));
		}
	}

	@Override
	public Tree getEntityNaviData() {

		Tree tree = Tree.make();

		for (FieldService fld : this.getEntityFieldsForNaviTree()) {
			DemsyEntityFieldService demsyFld = (DemsyEntityFieldService) fld;
			Node node = tree.addNode(null, fld.getPropName()).setName(fld.getName());
			node.set("open", "true");

			boolean success = this.makeNodes(tree, node, demsyFld.getEntity());
			if (!success) {
				tree.removeNode(node);
			}
		}

		// 如果导航树节点总数没有超过边框则全部展开
		// root.optimizeStatus();

		tree.sort();

		return tree;
	}

	@Override
	public Tree getEntityTreeData() {

		Tree tree = Tree.make();

		for (FieldService fld : this.getEntityFieldsForSelfTree()) {
			DemsyEntityFieldService demsyFld = (DemsyEntityFieldService) fld;
			Node node = tree.addNode(null, fld.getPropName()).setName(fld.getName());
			node.set("open", "true");

			boolean success = this.makeDataNodes(tree, node, demsyFld.getEntity());
			if (!success) {
				tree.removeNode(node);
			}
		}

		// 如果导航树节点总数没有超过边框则全部展开
		tree.optimizeStatus();

		tree.sort();

		return tree;
	}

	private CndExpr makeSortExpr(IEntityModule table, String type) {
		CndExpr ret = null;

		String sortExpr = table.getDataSortExpr();
		String[] exprs = StringUtil.toArray(sortExpr, ",;，；\t\r\n");

		for (String expr : exprs) {
			String[] arr = StringUtil.toArray(expr, ":");
			if (arr.length > 1) {
				if (!type.equalsIgnoreCase(arr[0].trim()))
					continue;

				String treeExpr = arr[1];
				int idx = treeExpr.indexOf(" ");
				if (idx > -1) {
					String sortFld = treeExpr.substring(0, idx);
					String sortType = treeExpr.substring(idx + 1);
					if ("DESC".equalsIgnoreCase(sortType.trim()))
						ret = CndExpr.desc(sortFld.trim());
					else
						ret = CndExpr.asc(sortFld.trim());
				} else {
					ret = CndExpr.asc(treeExpr.trim());
				}
			}
		}

		return ret;
	}

	private boolean makeDataNodes(Tree tree, Node node, EntityColumn field) {
		BizEngine bizEngine = (BizEngine) Demsy.entityModuleManager;

		DemsyEntityFieldService cocField = new DemsyEntityFieldService(field);

		String parentNodeID = node == null ? "" : node.getId();

		if (bizEngine.isSystemFK(field)) {

			// 获取该字段引用的外键系统
			IEntityModule fkSystem = field.getRefrenceSystem();

			// 查询外键数据
			ExtOrm orm = Demsy.orm();
			Class fkSystemType = bizEngine.getType(fkSystem);
			// if (orm.count(fkSystemType) > 50) {
			// return false;
			// }
			List fkSystemRecords = orm.query(fkSystemType, makeSortExpr((IEntityModule) fkSystem, "tree"));

			// 数据自身树
			String selfTreeProp = null;
			IEntityColumn selfTreeFld = bizEngine.getFieldOfSelfTree(fkSystem);
			if (selfTreeFld != null) {
				selfTreeProp = bizEngine.getPropName(selfTreeFld);
			}

			String parentID = "";
			for (Object record : fkSystemRecords) {
				// 计算上级节点ID
				if (!StringUtil.isNil(selfTreeProp)) {
					Object parentObj = ObjectUtil.getValue(record, selfTreeProp);
					if (parentObj != null)
						parentID = "" + ObjectUtil.getValue(parentObj, "id");
					else
						parentID = "";
				}

				// 计算节点ID
				String nodeID = null;
				if (ClassUtil.hasField(fkSystemType, "id")) {
					nodeID = "" + ObjectUtil.getValue(record, "id");
				} else {
					nodeID = "" + record.hashCode();
				}

				// 计算节点名称
				String nodeName = record.toString();

				// 添加节点
				Node childNode = tree.addNode(StringUtil.isNil(parentID) ? parentNodeID : parentID, nodeID).setName(nodeName);

				// 计算节点顺序
				if (ClassUtil.hasField(fkSystemType, "orderby")) {
					Integer seq = ObjectUtil.getValue(record, "orderby");
					if (seq != null)
						childNode.setSequence(seq);
				}
			}
		} else {
			KeyValue[] options = cocField.getDicOptions();
			if (options == null || options.length == 0 || options.length > 200) {
				return false;
			}

			for (KeyValue option : options) {
				tree.addNode(parentNodeID, option.getValue()).setName(option.getKey());
			}
		}

		return true;
	}

	private boolean makeNodes(Tree tree, Node node, EntityColumn field) {
		BizEngine bizEngine = (BizEngine) Demsy.entityModuleManager;

		DemsyEntityFieldService cocField = new DemsyEntityFieldService(field);

		String parentNodeID = node == null ? "" : node.getId();
		String propName = cocField.getPropName();

		if (bizEngine.isSystemFK(field)) {

			// 获取该字段引用的外键系统
			IEntityModule fkSystem = field.getRefrenceSystem();

			// 查询外键数据
			ExtOrm orm = Demsy.orm();
			Class fkSystemType = bizEngine.getType(fkSystem);
			// if (orm.count(fkSystemType) > 50) {
			// return false;
			// }
			List fkSystemRecords = orm.query(fkSystemType, makeSortExpr(fkSystem, "tree"));

			// 数据自身树
			String selfTreeProp = null;
			IEntityColumn selfTreeFld = bizEngine.getFieldOfSelfTree(fkSystem);
			if (selfTreeFld != null) {
				selfTreeProp = bizEngine.getPropName(selfTreeFld);
			}

			String parentID = "";
			for (Object record : fkSystemRecords) {
				// 计算上级节点ID
				if (!StringUtil.isNil(selfTreeProp)) {
					Object parentObj = ObjectUtil.getValue(record, selfTreeProp);
					if (parentObj != null)
						parentID = propName + ".id:" + ObjectUtil.getValue(parentObj, "id");
					else
						parentID = "";
				}

				// 计算节点ID
				String nodeID = null;
				if (ClassUtil.hasField(fkSystemType, "id")) {
					nodeID = "" + ObjectUtil.getValue(record, "id");
				} else {
					nodeID = "" + record.hashCode();
				}

				// 计算节点名称
				String nodeName = record.toString();

				// 添加节点
				Node childNode = tree.addNode(StringUtil.isNil(parentID) ? parentNodeID : parentID, propName + ".id:" + nodeID).setName(nodeName);

				// 计算节点顺序
				if (ClassUtil.hasField(fkSystemType, "orderby")) {
					Integer seq = ObjectUtil.getValue(record, "orderby");
					if (seq != null)
						childNode.setSequence(seq);
				}
			}
		} else {
			KeyValue[] options = cocField.getDicOptions();
			if (options == null || options.length == 0 || options.length > 200) {
				return false;
			}

			for (KeyValue option : options) {
				tree.addNode(parentNodeID, propName + ":" + option.getValue()).setName(option.getKey());
			}
		}

		return true;
	}

	public EntityModule getEntity() {
		return entity;
	}

	private Map<String, String> getFieldsModeMap(String opMode, Object data) {
		List<FieldService> fields = getEntityFields();
		Map<String, String> fieldMode = new HashMap();
		for (FieldService f : fields) {
			DemsyEntityFieldService field = (DemsyEntityFieldService) f;

			String mode = field.getMode(opMode);

			// String cascadeMode = this.getCascadeMode(field, entity)[1];
			// if (getModeValue(mode) < getModeValue(cascadeMode)) {
			// mode = cascadeMode;
			// }

			fieldMode.put(field.getPropName(), mode);
		}

		return fieldMode;
	}

	public void validateEntityData(String opMode, Object data) throws CocException {
		Map<String, String> fieldsMode = this.getFieldsModeMap(opMode, data);

		Iterator<String> keys = fieldsMode.keySet().iterator();
		List<String> requiredFields = new LinkedList();
		while (keys.hasNext()) {
			String field = keys.next();
			String mode = fieldsMode.get(field);
			if (mode != null && mode.indexOf("M") > -1) {
				Object v = ObjectUtil.getValue(data, field);
				if (v == null) {
					requiredFields.add(field);
				} else if (v instanceof String) {
					if (StringUtil.isNil((String) v))
						requiredFields.add(field);
				} else if (Obj.isEntity(v)) {
					if (Obj.isEmpty(null, v))
						requiredFields.add(field);
				}
			}
		}

		if (requiredFields.size() > 0) {
			Map<String, FieldService> fields = getBizFieldsMapByPropName();
			StringBuffer sb = new StringBuffer();
			for (String prop : requiredFields) {
				sb.append(',').append(fields.get(prop).getName());
			}
			throw new CocException("必填字段：[%s]", sb.toString().substring(1));
		}
	}

	public void set(String key, String value) {
		this.extProps.put(key, value);
	}

	@Override
	public Map<String, FieldService> getEntityFieldsPropMap() {
		Map<String, FieldService> ret = new Hashtable();
		for (FieldService f : this.bizFields) {
			ret.put(f.getPropName(), f);
		}

		return ret;
	}

	@Override
	public List parseEntityDataFrom(File excel) {
		try {
			SystemExcel systemExcel = new SystemExcel(entity, excel);
			return systemExcel.getRows();
		} catch (Throwable e) {
			throw new CocException(e.getMessage());
		}

	}
}
