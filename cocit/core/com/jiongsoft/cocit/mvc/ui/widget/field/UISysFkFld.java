package com.jiongsoft.cocit.mvc.ui.widget.field;

import java.io.Serializable;
import java.util.Map;

import com.jiongsoft.cocit.lang.Nodes;

public class UISysFkFld extends UIBizFld {

	private Nodes options;

	public UISysFkFld(Map ctx, Serializable id) {
		super(ctx, id);
	}

	public Nodes getOptions() {
		return options;
	}

	public UISysFkFld setOptions(Nodes options) {
		this.options = options;

		return this;
	}

}
