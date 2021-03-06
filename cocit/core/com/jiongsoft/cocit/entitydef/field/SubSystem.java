package com.jiongsoft.cocit.entitydef.field;

import java.util.ArrayList;
import java.util.List;

import com.jiongsoft.cocit.lang.DemsyException;
import com.jiongsoft.cocit.lang.JSON;
import com.jiongsoft.cocit.lang.Str;
import com.jiongsoft.cocit.log.Log;
import com.jiongsoft.cocit.log.Logs;
import com.jiongsoft.cocit.util.sort.SortUtils;
import com.kmjsoft.cocit.entity.INamedEntity;

/**
 * 子系统数据：将被自动保存到子系统数据表中
 * 
 * @author yongshan.ji
 * 
 * @param <T>
 */
public class SubSystem<T> implements IExtField {
	private static Log log = Logs.get();

	private Class<T> type;

	private String jsonData;

	private List<T> list;

	public SubSystem() {
		this("");
	}

	public SubSystem(String str) {
		init(str);
	}

	public SubSystem(List<T> list) {
		if (list != null)
			SortUtils.sort(list, "orderby", true);

		this.list = list;
	}

	public SubSystem(String str, Class<T> type) {
		this.type = type;
		init(str);
	}

	private void init(String str) {
		if (Str.isEmpty(jsonData)) {
			this.jsonData = "";
		}
		this.jsonData = str;

		if (list == null && type != null) {
			list = new ArrayList();
			if (!Str.isEmpty(jsonData)) {
				try {
					list = JSON.loadFromJson(type, jsonData);
				} catch (Throwable e) {
					log.errorf("从JSON加载子系统数据出错! %s", e);
					// throw new DemsyException(e);
				}
			}
		}
	}

	public List<T> getList() {
		return list;
	}

	public List<T> getList(Class type) {
		List list = new ArrayList();
		if (!Str.isEmpty(jsonData)) {
			try {
				list = JSON.loadFromJson(type, jsonData);
			} catch (Throwable e) {
				log.errorf("从JSON加载子系统数据出错! %s", e);
				throw new DemsyException(e);
			}
		}
		if (list != null) {
			int orderby = 1;
			for (Object obj : list) {
				if (obj instanceof INamedEntity) {
					((INamedEntity) obj).setSerialNumber(orderby++);
				}
			}
		}

		return list;
	}

	public String toString() {
		return jsonData == null ? "" : jsonData;
	}

	public String toJson() {
		return JSON.toJson(jsonData);
	}
}
