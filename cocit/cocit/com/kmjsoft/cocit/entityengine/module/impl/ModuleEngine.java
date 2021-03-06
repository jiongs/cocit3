package com.kmjsoft.cocit.entityengine.module.impl;

import static com.kmjsoft.cocit.Demsy.appconfig;
import static com.kmjsoft.cocit.Demsy.entityModuleManager;
import static com.kmjsoft.cocit.Demsy.security;
import static com.kmjsoft.cocit.entity.EntityConst.BIZSYS_DEMSY_DATASOURCE;
import static com.kmjsoft.cocit.entity.EntityConst.BIZSYS_DEMSY_LIB_ACTION;
import static com.kmjsoft.cocit.entity.EntityConst.BIZSYS_DEMSY_SOFT;
import static com.kmjsoft.cocit.entity.EntityConst.F_BUILDIN;
import static com.kmjsoft.cocit.entity.EntityConst.F_ORDER_BY;
import static com.kmjsoft.cocit.entityengine.manager.BizConst.TYPE_BZFORM_NEW;
import static com.kmjsoft.cocit.entityengine.manager.BizConst.TYPE_BZSYS;
import static com.kmjsoft.cocit.entityengine.manager.BizConst.TYPE_BZ_AUTO_MAKED_UPDATE_MENUS;
import static com.kmjsoft.cocit.entityengine.manager.BizConst.TYPE_BZ_SAVE;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.dao.Sqls;
import org.nutz.lang.Mirror;

import com.jiongsoft.cocit.config.IDataSourceConfig;
import com.jiongsoft.cocit.entitydef.field.Upload;
import com.jiongsoft.cocit.lang.Assert;
import com.jiongsoft.cocit.lang.Cls;
import com.jiongsoft.cocit.lang.DemsyException;
import com.jiongsoft.cocit.lang.Nodes;
import com.jiongsoft.cocit.lang.Obj;
import com.jiongsoft.cocit.lang.Option;
import com.jiongsoft.cocit.lang.Str;
import com.jiongsoft.cocit.lang.Nodes.Node;
import com.jiongsoft.cocit.log.Log;
import com.jiongsoft.cocit.log.Logs;
import com.jiongsoft.cocit.mvc.MvcConst;
import com.jiongsoft.cocit.mvc.MvcConst.MvcUtil;
import com.jiongsoft.cocit.util.sort.SortUtils;
import com.kmjsoft.cocit.Demsy;
import com.kmjsoft.cocit.entity.EntityConst;
import com.kmjsoft.cocit.entity.IDataEntity;
import com.kmjsoft.cocit.entity.INamedEntity;
import com.kmjsoft.cocit.entity.actionplugin.IActionPlugin;
import com.kmjsoft.cocit.entity.config.ITenantPreference;
import com.kmjsoft.cocit.entity.module.IEntityAction;
import com.kmjsoft.cocit.entity.module.IEntityColumn;
import com.kmjsoft.cocit.entity.module.IEntityModule;
import com.kmjsoft.cocit.entity.security.IFunMenu;
import com.kmjsoft.cocit.entity.security.ISystem;
import com.kmjsoft.cocit.entity.security.ITenant;
import com.kmjsoft.cocit.entityengine.service.SecurityManager;
import com.kmjsoft.cocit.funmenu.IFunMenuManager;
import com.kmjsoft.cocit.orm.ExtOrm;
import com.kmjsoft.cocit.orm.expr.CndExpr;
import com.kmjsoft.cocit.orm.nutz.EnMappingImpl;
import com.kmjsoft.cocit.orm.nutz.impl.OrmImpl;
import com.kmjsoft.cocit.util.UrlAPI;

public abstract class ModuleEngine implements IFunMenuManager {
	protected Log log = Logs.getLog(this.getClass());

	protected Map<String, CacheCorp> corpCache;

	protected Map<String, CacheSoft> softCache;

	protected Map<Long, CacheSoft> softIdCache;

	protected Map<Long, IEntityAction> actionLibCache;

	protected Map<Long, IDataSourceConfig> dataSourceCache;

	protected Map<Long, IActionPlugin[]> iActionPlugins;

	public ModuleEngine() {
		corpCache = new HashMap();
		softCache = new HashMap();
		softIdCache = new HashMap();
		actionLibCache = new HashMap();
		iActionPlugins = new HashMap();
	}

	@Override
	public IActionPlugin[] getPlugins(IEntityAction entityAction) {
		IActionPlugin[] ret = iActionPlugins.get(entityAction.getId());
		if (ret != null)
			return ret;

		String[] pluginArray = Str.toArray(entityAction.getPlugin(), ",");
		List<IActionPlugin> plugins = new ArrayList(pluginArray.length);
		for (String pstr : pluginArray) {
			try {
				IActionPlugin plugin = (IActionPlugin) Mirror.me(Cls.forName(pstr)).born();
				plugins.add(plugin);
			} catch (Throwable e) {
				log.errorf("加载业务插件出错! [action=%s] %s", entityAction, e);
			}
		}

		ret = new IActionPlugin[plugins.size()];
		plugins.toArray(ret);
		iActionPlugins.put(entityAction.getId(), ret);

		return ret;
	}

	public void clearCache() {
		synchronized (ModuleEngine.class) {
			corpCache.clear();
			softCache.clear();
			softIdCache.clear();
			actionLibCache.clear();
			dataSourceCache = null;
		}
	}

	// protected IOrm orm() {
	// return Demsy.orm();
	// }

	@Override
	public IEntityModule getSystem(IFunMenu funMenu) {
		if (funMenu != null && funMenu.getType() == IFunMenu.TYPE_ENTITY) {
			return entityModuleManager.getSystem(funMenu.getRefID());
		}

		return null;
	}

	@Override
	public ISystem getCorpByDefault() {
		return getCorp(appconfig.getDefaultCorpCode());
	}

	@Override
	public ISystem getCorp(String code) {
		return corp(code).get();
	}

	CacheCorp corp(String code) {
		CacheCorp corp = corpCache.get(code);
		if (corp == null)
			corp = new CacheCorp(this, code);

		return corp;
	}

	@Override
	public ITenant getSoftByDefault() {
		return soft().get();
	}

	@Override
	public ITenant getSoft(String domainOrCode) {
		if (Str.isEmpty(domainOrCode) || domainOrCode.equals("localhost") || domainOrCode.equals("127.0.0.1")) {
			domainOrCode = appconfig.getDefaultSoftCode();
		}
		return soft(domainOrCode).get();
	}

	@Override
	public ITenant getSoft(Long id) {
		return soft(id).get();
	}

	protected CacheSoft soft() {
		return soft(appconfig.getDefaultSoftCode());
	}

	protected CacheSoft soft(String domainOrCode) {
		CacheSoft ret = softCache.get(domainOrCode);
		if (ret == null)
			ret = new CacheSoft(this, domainOrCode);

		return ret;
	}

	protected CacheSoft soft(Long id) {
		CacheSoft ret = softIdCache.get(id);
		if (ret == null)
			ret = new CacheSoft(this, id);

		return ret;
	}

	public ITenantPreference getSoftConfig(String key) {
		return soft().configs().get(key);
	}

	@Override
	public IFunMenu getModule(Long moduleID) {
		return soft().funMenu(moduleID);
	}

	@Override
	public IFunMenu getModule(String moduleGuid) {
		return soft().funMenu(moduleGuid);
	}

	@Override
	public IFunMenu getModule(ITenant soft, IEntityModule system) {
		if (soft == null) {
			soft = Demsy.me().getTenant();
		}
		return this.getModule(soft.getDataGuid(), system);
	}

	public IFunMenu getModule(String tenantGuid, IEntityModule system) {
		if (system == null)
			return null;
		return soft(tenantGuid).bizModule(system.getId());
	}

	@Override
	public List<IFunMenu> getSubModules(IFunMenu funMenu) {
		List<IFunMenu> funMenus = new LinkedList();

		List<? extends IEntityModule> slaveSystems = entityModuleManager.getSystemsOfSlave(getSystem(funMenu));
		for (IEntityModule sys : slaveSystems) {
			IFunMenu slaveModule = this.getModule(funMenu.getTenantOwnerGuid(), sys);
			if (slaveModule != null) {
				funMenus.add(slaveModule);
			}
		}

		return funMenus;
	}

	@Override
	public IEntityAction getAction(IFunMenu mdl, Serializable opID) {
		if (mdl == null)
			return null;

		if (mdl.getType() == IFunMenu.TYPE_ENTITY) {
			if (opID instanceof String)
				return ((BizEngine) entityModuleManager).biz(getSystem(mdl).getId()).action((String) opID);
			else if (opID instanceof Number)
				return ((BizEngine) entityModuleManager).biz(getSystem(mdl).getId()).action(((Number) opID).longValue());
			else
				return null;
		}

		return null;

	}

	@Override
	public List<? extends IFunMenu> getModules(ITenant soft) {
		return soft(soft.getId()).funMenus();
	}

	@Override
	public Nodes makeNodesByModule(ITenant soft) {
		return this.makeNodesByModule(soft, Demsy.me().login().getRoleType());
	}

	@Override
	public Nodes makeNodesByModule(ITenant soft, byte role) {
		Assert.notNull(soft, "没有指定应用软件，不能获取模块功能菜单!");

		List<? extends IFunMenu> funMenus = soft(soft.getId()).funMenus();

		Nodes root = Nodes.make();
		for (IFunMenu funMenu : funMenus) {
			if (funMenu.isDisabled() || funMenu.isHidden())
				continue;

			if (!security.allowVisitModule(funMenu, true))
				continue;

			makeNode(root, funMenu);
		}

		SortUtils.sort(root.getChildren(), "order", true);

		// 非超级用户不能访问“平台管理”功能
		if (role != SecurityManager.ROLE_DP_SUPPORT) {
			List<Node> list = root.getChildren();
			for (int i = list.size() - 1; i >= 0; i--) {
				Node node = list.get(i);
				String code = (String) node.getString("code");
				if (code != null)
					code = code.trim();
				if (EntityConst.BIZCATA_DEMSY_ADMIN.equals(code)) {
					list.remove(i);
				}
			}

			root.optimize();
		}

		return root;
	}

	private void makeNode(Nodes root, IFunMenu funMenu) {
		if (funMenu.isDisabled() || funMenu.isHidden())
			return;

		Node node;
		if (funMenu.getParent() == null) {
			node = root.addNode(null, funMenu.getId());
		} else {
			makeNode(root, (IFunMenu) funMenu.getParent());

			node = root.addNode(((INamedEntity) funMenu.getParent()).getId(), funMenu.getId());
		}
		// node.setParams(moduleID);
		node.setOrder(funMenu.getSerialNumber());
		node.set("code", funMenu.getCode());
		node.set("moduleID", funMenu);

		node.setName(funMenu.getName());

		String pathPrefix = funMenu.getPathPrefix();

		switch (funMenu.getType()) {
		case IFunMenu.TYPE_FOLDER:
			break;
		case IFunMenu.TYPE_ENTITY:
			if (UrlAPI.URL_NS.equals(pathPrefix)) {
				node.setParams(MvcUtil.contextPath(UrlAPI.GET_ENTITY_MODULE_UI, UrlAPI.encodeArgs(funMenu.getId())));
			} else {
				node.setParams(MvcUtil.contextPath(MvcConst.URL_BZMAIN, funMenu.getId()));
			}
			break;
		case IFunMenu.TYPE_STATIC:
			node.setParams(MvcUtil.contextPath(funMenu.getPath(), funMenu.getId()));
			break;
		case IFunMenu.TYPE_WEB:
			break;
		}
	}

	@Override
	public Nodes makeNodesByAction(IFunMenu mdl) {
		if (mdl.getType() == IFunMenu.TYPE_ENTITY) {
			IEntityModule sys = getSystem(mdl);
			Nodes root = makeActionNodes(mdl, sys);

			// List<Node> list = root.getChildren();
			// 移除从系统的新增操作
			// for (int i = list.size() - 1; i >= 0; i--) {
			// Node node = list.get(i);
			// if (node.getType().equals(TYPE_BZ_NEW) && bizEngine.isSlave(sys))
			// {
			// list.remove(i);
			// }
			// }

			String softID = mdl.getTenantOwnerGuid();
			List<? extends IEntityColumn> fieldsOfSlave = entityModuleManager.getFieldsOfSlave(sys);

			// 添加子系统“新增”操作到主系统操作菜单中
			// if (fields.size() > 0) {
			// for (IBizField f : fields) {
			// String param = bizEngine.getPropName(f);
			// if
			// (bizEngine.getType(f).equals(bizEngine.getType(f.getRefrenceSystem())))
			// {
			// param += ".id";
			// }
			//
			// IBizSystem subsys = f.getSystem();
			// IModule submdl = this.getModule(softID, subsys);
			// if (submdl == null) {
			// log.warnf("业务模块不存在! [system=%s]", subsys);
			// continue;
			// }
			//
			// Nodes submdlRoot = makeActionNodes(submdl, subsys);
			// List<Node> nodes = submdlRoot.getChildren();
			// for (Node node : nodes) {
			// if (node.getType().equals(TYPE_BZFORM_NEW)) {
			// node.setParams(param);
			// node.set("masterModuleID", mdl.getId());
			// root.addChild(node);
			// }
			// }
			// }
			// }

			List<Node> children = root.getChildren();

			// 自动生成字段字段批量修改菜单
			List<Node> items = this.filterNodes(children, TYPE_BZ_AUTO_MAKED_UPDATE_MENUS);// 过滤自动生成菜单项
			if (items.size() > 0) {
				Map<String, IEntityColumn> fldsMap = entityModuleManager.getFieldsMap(entityModuleManager.getFieldsOfNavi(sys));
				Node unknownItem = null;

				List<String> props = new LinkedList();
				for (Node item : items) {
					if (Str.isEmpty((String) item.getParams())) {
						if (unknownItem != null) {
							children.remove(item);
						} else {
							unknownItem = item;
						}
						continue;
					}
					List<String> propNames = Str.toList((String) item.getParams(), ",");
					for (String prop : propNames) {
						if (props.contains(prop) || prop.equals(F_BUILDIN)) {
							continue;
						}
						props.add(prop);

						makeUpdateMenu(mdl, fldsMap.get(prop), root, item);

						fldsMap.remove(prop);
					}
					if (item.getSize() == 0) {
						children.remove(item);
					}
				}

				if (unknownItem != null) {
					int count = 1;

					Iterator<IEntityColumn> it = fldsMap.values().iterator();
					while (it.hasNext()) {
						IEntityColumn fld = it.next();

						if (fld.getPropName().equals(F_BUILDIN)) {
							continue;
						}

						Node item = root.addNode(unknownItem.getId(), unknownItem.getId() + "_" + (count++)).setName(unknownItem.getName() + fld.getName());
						item.set("mode", unknownItem.getString("mode"));

						if (!entityModuleManager.isSystemFK(fld)) {
							makeUpdateMenu(mdl, fld, root, item);
						}

						if (item.getSize() == 0) {
							unknownItem.getChildren().remove(item);
						}
					}
					if (unknownItem.getSize() == 1) {
						int index = children.indexOf(unknownItem);
						children.add(index, unknownItem.getChildren().get(0));
						children.remove(unknownItem);
					}
					if (unknownItem.getSize() == 0) {
						children.remove(unknownItem);
					}
				}
			}

			// 合并“新增”菜单组
			// items = this.filterNodes(children, TYPE_BZFORM_NEW);// 过滤新增菜单项
			// if (items.size() > 1 && children.size() > 20) {
			// List<Node> newnodes = root.getChildren();
			// Node newGroup = root.addNode(null, "new",
			// 0).setName("新增").setIcon(items.get(0).getIcon());
			// newnodes = newGroup.getChildren();
			// for (Node item : items) {
			// children.remove(item);
			// newnodes.add(item);
			// }
			// }

			// 生成子系统菜单组
			if (fieldsOfSlave.size() > 0) {
				String groupsSubMdlID = null;
				if (fieldsOfSlave.size() + root.getSize() > 20) {
					groupsSubMdlID = "grpssubmdl_" + mdl.getId();
					root.addNode(null, groupsSubMdlID).setName("明细数据");
				}
				for (IEntityColumn f : fieldsOfSlave) {
					String param = entityModuleManager.getPropName(f) + ".id";

					IEntityModule subsys = f.getSystem();
					IFunMenu submdl = this.getModule(softID, subsys);
					if (submdl == null || !security.allowVisitModule(submdl, true)) {
						log.warnf("业务模块不存在! [system=%s]", subsys);
						continue;
					}

					// 子模块菜单组
					String groupSubMdlID = "grpsubmdl_" + submdl.getId();
					Node groupSubMdl = root.addNode(groupsSubMdlID, groupSubMdlID).setName(submdl.getName());

					// 子模块菜单项——“添加”
					Nodes submdlRoot = makeActionNodes(submdl, subsys);
					List<Node> subnodes = submdlRoot.getChildren();
					for (Node node : subnodes) {
						if (node.getType().equals(TYPE_BZFORM_NEW)) {
							node.setParams(param);
							node.set("masterModuleID", mdl.getId());
							groupSubMdl.getChildren().add(node);
						}
					}

					// 子模块菜单项——“查看”
					String nodeIdViewSubMdl = "viewsubmdl_" + submdl.getId();
					Node nodeViewSubMdl = root.addNode(groupSubMdlID, nodeIdViewSubMdl);
					nodeViewSubMdl.set("moduleID", submdl.getId());
					nodeViewSubMdl.set("masterModuleID", mdl.getId());
					nodeViewSubMdl.setName("查看明细");
					nodeViewSubMdl.setType(TYPE_BZSYS);
					nodeViewSubMdl.setParams(param);
				}
			}

			return root;
		}

		throw new java.lang.UnsupportedOperationException("不支持的模块类型!");
	}

	private void makeUpdateMenu(IFunMenu mdl, IEntityColumn fld, Nodes root, Node item) {
		String prop = entityModuleManager.getPropName(fld);
		if (fld == null) {
			return;
		}
		if (entityModuleManager.isSystemFK(fld)) {
			try {
				Class klass = entityModuleManager.getGenericType(fld);
				IEntityModule refSys = fld.getRefrenceSystem();
				if (!Cls.isEntityType(klass)) {
					klass = entityModuleManager.getType(refSys);
				}
				ExtOrm orm = Demsy.orm();
				Class type = entityModuleManager.getType(refSys);

				if (orm.count(type, null) < 10) {
					List<? extends IDataEntity> datas = orm.query(type, Cls.hasField(klass, F_ORDER_BY) ? CndExpr.asc(F_ORDER_BY) : null);
					for (IDataEntity data : datas) {
						Node node = root.addNode(item.getId(), item.getId() + "_" + fld.getId() + "_" + data.getId());
						node.setName(data.toString());
						node.setType(TYPE_BZ_SAVE);
						node.setParams(prop + ".id=" + data.getId());
						node.set("moduleID", mdl.getId());
						node.set("mode", item.getString("mode"));
					}
				}

			} catch (DemsyException e) {
				log.errorf("自动生成修改菜单出错! [%s(%s)] %s", fld.getName(), prop, e);
			}
		} else {
			String post = entityModuleManager.isV1Dic(fld) ? ".id" : "";
			Option[] options = entityModuleManager.getOptions(fld);
			for (Option option : options) {
				Node node = root.addNode(item.getId(), item.getId() + "_" + fld.getId() + "_" + option.getValue());
				node.setName(option.getText());
				node.setType(TYPE_BZ_SAVE);
				node.setParams(prop + post + "=" + option.getValue());
				node.set("moduleID", mdl.getId());
				node.set("mode", item.getString("mode"));
			}
		}
	}

	private List<Node> filterNodes(List<Node> list, int type) {
		List<Node> items = new LinkedList();
		for (Node node : list) {
			if (node.getType() != null && node.getType().equals(type)) {
				items.add(node);
			}
		}
		return items;
	}

	protected String makeParent(String parent, Long id) {
		if (Str.isEmpty(parent)) {
			return id + ".";
		}

		if (parent.endsWith("."))
			return parent + id + ".";
		else
			return parent + "." + id + ".";
	}

	public Nodes makeNodesByCurrentSoft() {

		Nodes root = Nodes.make();
		try {
			List<? extends ITenant> list = Demsy.orm().query(entityModuleManager.getStaticType(BIZSYS_DEMSY_SOFT));

			for (ITenant ele : list) {
				ISystem corp = ele.getSystem();
				if (corp == null)
					continue;

				Node corpNode = root.addNode(null, "corp_" + corp.getId());
				corpNode.setName(corp.getName());

				Node softNode = root.addNode(corpNode.getId(), ele.getId());
				softNode.setName(ele.getName()).setParams(ele);
			}
		} catch (Throwable e) {
			log.warn(e);
		}
		return root;
	}

	// public Nodes makeNodesByRealm(ITenant soft) {
	// return makeComNodes(this.getRealms(soft));
	// }

	public Nodes makeComNodes(List<? extends INamedEntity> list) {
		Nodes root = Nodes.make();

		if (list != null)
			for (INamedEntity ele : list) {
				Node node = root.addNode(null, ele.getId());
				node.setName(ele.getName()).setParams(ele.getCode());
			}

		return root;
	}

	private Nodes makeActionNodes(IFunMenu funMenu, IEntityModule system) {
		List<? extends IEntityAction> list = ((BizEngine) entityModuleManager).biz(system.getId()).actions();

		Nodes root = Nodes.make();

		// 添加业务操作菜单项到根节点F
		for (IEntityAction action : list) {
			if (action.isDisabled() || Str.isEmpty(action.getName()))
				continue;

			IEntityAction parent = action.getParentAction();

			Node node = root.addNode(parent == null ? "" : parent.getId(), action.getId());
			node.setName(action.getName());
			node.setType(action.getTypeCode());
			node.setParams(action.getParams());
			node.set("moduleID", funMenu.getId());
			if (Str.isEmpty(action.getMode())) {
				node.set("mode", action.getId());
			} else {
				node.set("mode", action.getMode());
			}
			if (!Str.isEmpty(action.getTargetUrl())) {
				node.set("url", action.getTargetUrl());
			}
			if (!Str.isEmpty(action.getTargetWindow())) {
				node.set("target", action.getTargetWindow());
			}

			String logoPath = null;
			Upload logo = action.getLogo();
			if (logo == null || Str.isEmpty(logo.toString()))
				logoPath = Demsy.appconfig.get("imagepath.actionlib") + "/" + action.getMode() + ".gif";
			else
				logoPath = logo.toString();

			if (new File(Demsy.contextDir + logoPath).exists()) {
				node.setIcon(logoPath);
			}

		}

		return root;
	}

	//
	// public List<? extends IRealm> getRealms(ITenant softObj) {
	// if (softObj == null) {
	// return null;
	// }
	// return this.soft(softObj.getId()).realms();
	// }
	//
	// public IRealm getRealm(ITenant softObj, String realmCode) {
	// return this.soft(softObj.getId()).realm(realmCode);
	// }

	public IEntityAction getActionComponent(Long id) {
		if (this.actionLibCache.size() == 0) {
			List<IEntityAction> list = Demsy.orm().query(entityModuleManager.getStaticType(BIZSYS_DEMSY_LIB_ACTION));
			for (IEntityAction ele : list) {
				actionLibCache.put(ele.getId(), ele);
			}
		}

		return this.actionLibCache.get(id);
	}

	public IDataSourceConfig getDataSource(Long dataSource) {
		if (this.dataSourceCache == null) {
			dataSourceCache = new HashMap();
			List<INamedEntity> list = Demsy.orm().query(entityModuleManager.getStaticType(BIZSYS_DEMSY_DATASOURCE));
			for (INamedEntity ele : list) {
				dataSourceCache.put(ele.getId(), (IDataSourceConfig) ele);
			}
		}

		return this.dataSourceCache.get(dataSource);
	}

	public void increase(ExtOrm orm, Object obj, String field) {
		increase(orm, obj, field, 1);
	}

	public void increase(ExtOrm orm, Object obj, String field, int v) {
		EnMappingImpl en = (EnMappingImpl) orm.getEnMapping(obj.getClass());
		Serializable id = Obj.getId(en, obj);

		StringBuffer sb = new StringBuffer("update ");
		sb.append(en.getTableName()).append(" set ");
		String colname = en.getField(field).getColumnName();
		Number value = Obj.getValue(obj, field);
		if (value == null || value.intValue() == 0) {
			sb.append(colname).append("=").append(v);
		} else {
			sb.append(colname).append("=").append(colname).append("+").append(v);
		}
		sb.append(" where ").append(en.getIdentifiedField().getColumnName()).append("=").append(id);
		String sqlstr = sb.toString();

		((OrmImpl) orm).getDao().execute(Sqls.create(sqlstr));
	}

	public void decrease(ExtOrm orm, Object obj, String field) {
		decrease(orm, obj, field, 1);
	}

	public void decrease(ExtOrm orm, Object obj, String field, int v) {
		Number value = Obj.getValue(obj, field);
		if (value != null && value.intValue() > 0) {
			EnMappingImpl en = (EnMappingImpl) orm.getEnMapping(obj.getClass());
			Serializable id = Obj.getId(en, obj);

			StringBuffer sb = new StringBuffer("update ");
			sb.append(en.getTableName()).append(" set ");
			String colname = en.getField(field).getColumnName();
			sb.append(colname).append("=").append(colname).append("-").append(v);
			sb.append(" where ").append(en.getIdentifiedField().getColumnName()).append("=").append(id);
			String sqlstr = sb.toString();

			((OrmImpl) orm).getDao().execute(Sqls.create(sqlstr));
		}
	}
}
