package com.kmjsoft.cocit.orm.listener;

import java.util.List;

import com.kmjsoft.cocit.orm.mapping.EnMapping;
import com.kmjsoft.cocit.orm.nutz.IExtDao;

public class EntityListeners {

	private List<EntityListener> listeners;

	public List<EntityListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<EntityListener> listeners) {
		this.listeners = listeners;
	}

	public void insertBefore(IExtDao dao, EnMapping entity, Object obj) {
		if (listeners != null) {
			for (EntityListener l : listeners) {
				l.insertBefore(dao, entity, obj);
			}
		}
	}

	public void insertAfter(IExtDao dao, EnMapping entity, Object obj) {
		if (listeners != null) {
			for (EntityListener l : listeners) {
				l.insertAfter(dao, entity, obj);
			}
		}
	}

	public void updateBefore(IExtDao dao, EnMapping entity, Object obj) {
		if (listeners != null) {
			for (EntityListener l : listeners) {
				l.updateBefore(dao, entity, obj);
			}
		}
	}

	public void updateAfter(IExtDao dao, EnMapping entity, Object obj) {
		if (listeners != null) {
			for (EntityListener l : listeners) {
				l.updateAfter(dao, entity, obj);
			}
		}
	}

	public void deleteBefore(IExtDao dao, EnMapping entity, Object obj) {
		if (listeners != null) {
			for (EntityListener l : listeners) {
				l.deleteBefore(dao, entity, obj);
			}
		}
	}

	public void deleteAfter(IExtDao dao, EnMapping entity, Object obj) {
		if (listeners != null) {
			for (EntityListener l : listeners) {
				l.deleteAfter(dao, entity, obj);
			}
		}
	}
}
