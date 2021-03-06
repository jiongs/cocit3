package com.kmjsoft.cocit.entityengine.manager.impl;

import static com.kmjsoft.cocit.Demsy.entityModuleManager;
import static com.kmjsoft.cocit.Demsy.security;

import java.util.List;

import com.jiongsoft.cocit.lang.Cls;
import com.jiongsoft.cocit.lang.DemsyException;
import com.jiongsoft.cocit.log.Log;
import com.jiongsoft.cocit.log.Logs;
import com.kmjsoft.cocit.Demsy;
import com.kmjsoft.cocit.entity.actionplugin.IActionPlugin;
import com.kmjsoft.cocit.entity.module.IEntityAction;
import com.kmjsoft.cocit.entity.module.IEntityModule;
import com.kmjsoft.cocit.entity.security.IFunMenu;
import com.kmjsoft.cocit.entityengine.manager.IBizManager;
import com.kmjsoft.cocit.entityengine.manager.IBizSession;
import com.kmjsoft.cocit.orm.ExtOrm;
import com.kmjsoft.cocit.orm.Pager;
import com.kmjsoft.cocit.orm.expr.CndExpr;

/**
 * 业务逻辑
 * <p>
 * 处理基础模块业务逻辑
 * 
 * @author yongshan.ji
 */
public class BizManagerImpl implements IBizManager {
	protected Log log = Logs.get();

	// 业务会话：可以切换ORM
	protected IBizSession bizSession;

	protected IFunMenu bizModule;

	protected IEntityModule entityModule;

	protected Class classOfEntity;

	public IBizSession bizSession() {
		return bizSession;
	}

	public ExtOrm orm() {
		return bizSession.orm();
	}

	BizManagerImpl(IBizSession bizSession, IFunMenu funMenu, IEntityModule system) throws DemsyException {
		this.bizSession = bizSession;
		this.bizModule = funMenu;
		this.entityModule = system;
		this.classOfEntity = entityModuleManager.getType(entityModule);
	}

	BizManagerImpl(IBizSession bizSession, IEntityModule entityModule) throws DemsyException {
		this.bizSession = bizSession;
		this.entityModule = entityModule;
		this.classOfEntity = entityModuleManager.getType(entityModule);
	}

	BizManagerImpl(BizManagerImpl parent, ExtOrm orm) {
		this.bizSession = parent.bizSession.me(orm);
		this.bizModule = parent.bizModule;
		this.entityModule = parent.entityModule;
		this.classOfEntity = parent.classOfEntity;
	}

	@Override
	public IBizManager me(final ExtOrm orm) {
		class OrmBizManager extends BizManagerImpl {
			OrmBizManager() {
				super(BizManagerImpl.this, orm);
			}
		}
		return new OrmBizManager();
	}

	@Override
	public int save(Object entity, String actionID) throws DemsyException {
		this.checkPermission(entity, actionID);

		return bizSession.save(entity, this.buildCndExpr(null, actionID), loadPlugins(actionID));
	}

	@Override
	public int updateMore(Object obj, String actionID, CndExpr expr) throws DemsyException {
		this.checkPermission(obj, actionID);

		return bizSession.updateMore(obj, this.buildCndExpr(expr, actionID), loadPlugins(actionID));
	}

	public int delete(Object entity, String actionID) throws DemsyException {
		this.checkPermission(entity, actionID);

		return bizSession.delete(entity, loadPlugins(actionID));
	}

	@Override
	public int delete(Long id, String actionID) throws DemsyException {
		this.checkPermission(actionID);

		return bizSession.delete(this.getType(), id, loadPlugins(actionID));
	}

	@Override
	public int deleteMore(String actionID, CndExpr expr) throws DemsyException {
		this.checkPermission(actionID);

		return bizSession.deleteMore(classOfEntity, expr, loadPlugins(actionID));
	}

	@Override
	public Object load(Long id, String actionID) throws DemsyException {
		this.checkPermission(actionID);

		return bizSession.load(classOfEntity, id, this.buildCndExpr(null, actionID), loadPlugins(actionID));
	}

	public int count(String actionID, CndExpr expr) throws DemsyException {
		this.checkPermission(actionID);

		return bizSession.count(classOfEntity, this.buildCndExpr(expr, actionID), loadPlugins(actionID));
	}

	public List query(String actionID, CndExpr expr) throws DemsyException {
		this.checkPermission(actionID);

		return bizSession.query(classOfEntity, this.buildCndExpr(expr, actionID), loadPlugins(actionID));
	}

	@Override
	public List query(Pager pager, String actionID) throws DemsyException {
		this.checkPermission(actionID);

		pager.setQueryExpr(this.buildCndExpr(pager.getQueryExpr(), actionID));

		return bizSession.query(pager, loadPlugins(actionID));
	}

	@Override
	public Object run(Object obj, String actionID) throws DemsyException {
		return bizSession.run(obj, this.buildCndExpr(null, actionID), loadPlugins(actionID));
	}

	public void asynSave(Object entity, String actionID) throws DemsyException {
		this.checkPermission(entity, actionID);

		bizSession.asynSave(entity, buildCndExpr(null, actionID), loadPlugins(actionID));
	}

	@Override
	public void asynUpdateMore(Object obj, String actionID, CndExpr expr) throws DemsyException {
		this.checkPermission(obj, actionID);

		bizSession.asynUpdateMore(obj, expr, loadPlugins(actionID));
	}

	@Override
	public void asynDelete(Object obj, String actionID) throws DemsyException {
		this.checkPermission(obj, actionID);

		bizSession.asynDelete(obj, loadPlugins(actionID));
	}

	@Override
	public void asynDeleteMore(String actionID, CndExpr expr) throws DemsyException {
		this.checkPermission(actionID);

		bizSession.asynDeleteMore(classOfEntity, expr, loadPlugins(actionID));
	}

	@Override
	public void asynQuery(Pager pager, String actionID) throws DemsyException {
		this.checkPermission(actionID);

		pager.setQueryExpr(this.buildCndExpr(pager.getQueryExpr(), actionID));

		bizSession.asynQuery(pager, loadPlugins(actionID));
	}

	@Override
	public Object asynRun(Object obj, String actionID) throws DemsyException {

		return bizSession.asynRun(obj, buildCndExpr(null, actionID), loadPlugins(actionID));
	}

	public IFunMenu getModule() {
		return bizModule;
	}

	public IEntityModule getSystem() {
		return entityModule;
	}

	// ====================================================================
	// 权限控制
	// ====================================================================

	protected void checkPermission(String actionID) throws DemsyException {
	}

	protected void checkPermission(Object obj, String actionID) throws DemsyException {
		if (obj instanceof List) {
			for (Object ele : (List) obj) {
				this.checkPermission(ele, actionID);
			}
		} else {
			if (!Cls.getType(classOfEntity).isAssignableFrom(obj.getClass())) {
				throw new DemsyException("无权操作<%s>业务实体", obj.getClass().getSimpleName());
			}
			this.checkPermission(actionID);
		}
	}

	/**
	 * 创建查询条件、字段过滤、数据权限等条件表达式
	 * 
	 * @param expr
	 *            指定的条件表达式
	 * @param actionID
	 *            操作ID
	 * @return 新的条件表达式
	 */
	protected CndExpr buildCndExpr(CndExpr expr, String actionID) {
		// TODO: soft aware
		if (Demsy.security.isRootUser(Demsy.me().username())) {
			return expr;
		}

		// CndExpr softEq = Expr.eq(LibConst.F_SOFT_ID,
		// this.bizModule.getSoftID());
		// if (expr == null) {
		// return softEq;
		// }
		// return expr.and(softEq);

		CndExpr fkExpr = security.getDataFilter(bizModule);
		if (fkExpr != null) {
			if (expr == null)
				expr = fkExpr;
			else
				expr = expr.and(fkExpr);
		}

		return expr;
	}

	/**
	 * 加载模块业务插件
	 * 
	 * @return
	 */
	protected IActionPlugin[] loadPlugins(String actionID) {
		IEntityAction entityAction = Demsy.entityModuleManager.getAction(this.entityModule.getId(), actionID);

		if (entityAction == null)
			return null;

		return Demsy.funMenuManager.getPlugins(entityAction);
	}

	public Class getType() {
		return classOfEntity;
	}
}
