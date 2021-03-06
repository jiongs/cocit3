package com.jiongsoft.cocit.mvc.render;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.jiongsoft.cocit.lang.Obj;
import com.jiongsoft.cocit.lang.Str;
import com.jiongsoft.cocit.mvc.MvcConst;
import com.jiongsoft.cocit.mvc.template.ITemplateEngine;
import com.jiongsoft.cocit.mvc.ui.IUIView;
import com.jiongsoft.cocit.mvc.ui.model.UIBizGridModel;
import com.jiongsoft.cocit.mvc.ui.model.UIWidgetModel;
import com.jiongsoft.cocit.mvc.ui.widget.UIBizGrid;
import com.jiongsoft.cocit.mvc.ui.widget.UIBizGrid.UIGridFld;
import com.jiongsoft.cocit.mvc.ui.widget.field.UIBizFld;
import com.kmjsoft.cocit.Demsy;
import com.kmjsoft.cocit.orm.Pager;

/**
 * 
 * @author yongshan.ji
 */
public class DataRender extends TemplateRender implements MvcConst {
	public DataRender(ITemplateEngine tpl) {
		super(tpl);
	}

	public String getContentType(IUIView ui) {
		String type = ui.getDataType();

		if (type == null && ui instanceof UIWidgetModel) {
			type = ((UIWidgetModel) ui).getModel().getDataType();
		}

		if (type == DATA_JSON)
			return "application/json; charset=UTF-8";
		if (type == DATA_XML)
			return "text/xml; charset=UTF-8";

		return "text/html; charset=UTF-8";
	}

	protected String getTemplate(IUIView ui) {
		String tpl = ui.getTemplate();
		String datatype = ui.getDataType();
		String templateType = ui.getTemplateType();

		if (ui instanceof UIWidgetModel) {
			ui = ((UIWidgetModel) ui).getModel();
			if (tpl == null)
				tpl = ui.getTemplate();
			if (datatype == null)
				datatype = ui.getDataType();
			if (templateType == null)
				templateType = ui.getTemplateType();
		}

		if (Str.isEmpty(tpl)) {
			tpl = ui.getClass().getName().replace("com.kmetop.demsy.mvc.", "");
		}

		if (tpl.startsWith("/")) {
			if (tpl.endsWith(".st")) {
				return tpl;
			} else {
				return tpl + ".st";
			}
		}
		
		return (Demsy.appconfig.getTplPackage() + "." + tpl).replace(".", "/") + (Str.isEmpty(datatype) ? "" : ("." + datatype)) + templateType;
	}

	@Override
	public void render(Writer out, IUIView ui, Map context) throws Exception {
		if (ui instanceof UIBizGridModel) {
			UIBizGridModel grid = (UIBizGridModel) ui;
			this.render(out, grid.getModel(), grid.getData());

			return;
		}

		super.render(out, ui, context);
	}

	protected void render(Writer out, UIBizGrid model, Pager data) throws Exception {
		log.debugf("JAVA Render...[model=%s, type=xml]", model);

		out.write("<?xml version='1.0' encoding='UTF-8'?>");
		out.write("<rows>");
		out.write("<page>");
		out.write("" + data.getPageIndex());
		out.write("</page>");
		out.write("<total>");
		out.write("" + data.getTotalPage());
		out.write("</total>");
		out.write("<records>");
		out.write("" + data.getTotalRecord());
		out.write("</records>");
		List list = data.getResult();
		List<UIBizFld> flds = model.getFields();
		String idField = model.getIdField();
		int size = flds.size();
		for (Object obj : list) {
			out.write("<row>");
			out.write("<cell>");
			out.write(Obj.getStringValue(obj, idField));
			out.write("</cell>");
			for (int i = 0; i < size; i++) {
				UIGridFld fld = (UIGridFld) flds.get(i);
				if (fld.getHidden()) {
					continue;
				}
				out.write("<cell>");

				String str = "";
				if (Str.isEmpty(fld.getPattern())) {
					str = Obj.getStringValue(obj, fld.getPropName());
				} else {
					str = Obj.getStringValue(obj, fld.getPropName(), fld.getPattern());
				}
				if (fld.getOptions() != null && !Str.isEmpty(str)) {
					String tmp = fld.getOptions().get(str);
					if (!Str.isEmpty(tmp)) {
						str = tmp;
					}
				}
				if (fld.isString()) {
					out.write("<![CDATA[");
					out.write(Str.escapeHTML(str));
					out.write("]]>");
				} else {
					out.write(str);
				}
				out.write("</cell>");
			}
			out.write("\n</row>");
		}
		out.write("\n</rows>");

		log.debugf("JAVA Render: 结束. [model=%s, type=xml]", model);
	}
}
