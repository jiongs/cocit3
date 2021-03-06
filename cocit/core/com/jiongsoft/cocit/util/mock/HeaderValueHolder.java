package com.jiongsoft.cocit.util.mock;

/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jiongsoft.cocit.lang.Assert;
import com.jiongsoft.cocit.lang.Lists;

/**
 * Internal helper class that serves as value holder for request headers.
 * 
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0.1
 */
class HeaderValueHolder {

	private final List values = new LinkedList();

	public void setValue(Object value) {
		this.values.clear();
		this.values.add(value);
	}

	public void addValue(Object value) {
		this.values.add(value);
	}

	public void addValues(Collection values) {
		this.values.addAll(values);
	}

	public void addValueArray(Object values) {
		Lists.mergeArrayIntoCollection(values, this.values);
	}

	public List getValues() {
		return Collections.unmodifiableList(this.values);
	}

	public Object getValue() {
		return (!this.values.isEmpty() ? this.values.get(0) : null);
	}

	/**
	 * Find a HeaderValueHolder by name, ignoring casing.
	 * 
	 * @param headers
	 *            the Map of header names to HeaderValueHolders
	 * @param name
	 *            the name of the desired header
	 * @return the corresponding HeaderValueHolder, or <code>null</code> if none
	 *         found
	 */
	public static HeaderValueHolder getByName(Map headers, String name) {
		Assert.notNull(name, "Header name must not be null");
		for (Iterator it = headers.keySet().iterator(); it.hasNext();) {
			String headerName = (String) it.next();
			if (headerName.equalsIgnoreCase(name)) {
				return (HeaderValueHolder) headers.get(headerName);
			}
		}
		return null;
	}

}
