package com.kmjsoft.cocit.orm.nutz;

import org.nutz.dao.Dao;
import org.nutz.dao.entity.Link;
import org.nutz.dao.impl.DemsyInsertInvoker;
import org.nutz.lang.Mirror;

import com.jiongsoft.cocit.lang.Obj;
import com.kmjsoft.cocit.orm.mapping.EnMapping;

public class SaveOneInvoker extends DemsyInsertInvoker {

	public SaveOneInvoker(Dao dao, Object mainObj, Mirror<?> mirror) {
		super(dao, mainObj, mirror);
	}

	public void invoke(Link link, Object one) {
		if (Obj.isEmpty((EnMapping) dao.getEntity(one.getClass()), one)) {
			dao.insert(one);
		}
		// Mirror<?> ta = Mirror.me(one.getClass());
		// Object value = ta.getValue(one, link.getTargetField());
		// mirror.setValue(mainObj, link.getReferField(), value);
	}
}
