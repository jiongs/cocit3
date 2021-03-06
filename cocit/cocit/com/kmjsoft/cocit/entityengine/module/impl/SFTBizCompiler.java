package com.kmjsoft.cocit.entityengine.module.impl;

import com.kmjsoft.cocit.entity.module.IEntityModule;

public class SFTBizCompiler extends BizCompiler {

	SFTBizCompiler(BizEngine engine) {
		super(engine);
	}

	@Override
	protected String genSrcOfImport(IEntityModule system) {
		return new StringBuffer().append("\n")//
				.append("\nimport java.util.*;")//
				.append("\nimport java.math.*;")//
				.append("\nimport javax.persistence.*;")//
				.append("\nimport com.jiongsoft.cocit.entity.annotation.*;")//
				.append("\nimport com.kmetop.demsy.comlib.biz.field.*;")//
				.append("\nimport com.kmetop.demsy.comlib.impl.sft.*;")//
				.append("\nimport com.kmetop.demsy.comlib.impl.sft.dic.*;")//
				.append("\nimport com.kmetop.demsy.comlib.entity.base.*;")//
				.toString();
	}

}
