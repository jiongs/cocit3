// $codepro.audit.disable unnecessaryCast
package com.kmjsoft.cocit.entityengine.service.impl.demsy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.kmjsoft.cocit.Demsy;
import com.kmjsoft.cocit.entity.impl.module.EntityColumnGroup;
import com.kmjsoft.cocit.entityengine.module.impl.BizEngine;
import com.kmjsoft.cocit.entityengine.service.FieldGroupService;
import com.kmjsoft.cocit.entityengine.service.FieldService;

public class DemsyEntityGroupService implements FieldGroupService {
	private EntityColumnGroup entity;

	private List<FieldService> dataFields;

	DemsyEntityGroupService(EntityColumnGroup e) {
		this.entity = e;
		dataFields = new ArrayList();
	}

	void addField(FieldService f) {
		this.dataFields.add(f);
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
	// String value = entity.get(propName);
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
	public String getMode(String opCode) {

		return ((BizEngine) Demsy.entityModuleManager).parseMode(opCode, entity.getMode());

	}

	// @Override
	// public EntityTableService getDataTable() {
	// IBizSystem g = entity.getSystem();
	// if (g == null)
	// return null;
	//
	// return fc.makeDataTable(g.getId());
	// }

	@Override
	public List<FieldService> getEntityFields() {
		return this.dataFields;
	}

	public EntityColumnGroup getEntity() {
		return entity;
	}
}
