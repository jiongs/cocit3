package com.jiongsoft.cocit.entitydef.field;

import com.jiongsoft.cocit.lang.Str;
import com.kmjsoft.cocit.orm.annotation.CocColumn;

@CocColumn(name = "边框", uiTemplate = "ui.widget.field.Composite")
public class CssBorder extends JsonField<CssBorder> {

	@CocColumn(name = "边框宽度", SN = 1, uiTemplate = "ui.widget.field.Spinner")
	protected Integer width;

	@CocColumn(name = "边框风格", SN = 2, options = ":风格,solid:实线,dotted:点线,dashed:虚线,double:双线,groove:3D沟槽状,ridge:3D脊状,inset:3D内嵌边框,outset:3D外嵌")
	protected String style;

	@CocColumn(name = "边框颜色", SN = 3, uiTemplate = "ui.widget.field.CssColor")
	protected String color;

	@CocColumn(name = "边框间隙", SN = 4, uiTemplate = "ui.widget.field.Spinner")
	protected Integer padding;

	@CocColumn(name = "边框边距", SN = 5, uiTemplate = "ui.widget.field.Spinner")
	protected Integer margin;

	public CssBorder() {
		this("");
	}

	public CssBorder(String str) {
		super(str);
	}

	@Override
	protected void init(CssBorder obj) {
		if (obj != null) {
			this.width = obj.width;
			this.style = obj.style;
			this.color = obj.color;
			this.padding = obj.padding;
			this.margin = obj.margin;
		}
	}

	public String toCssStyle(String _post) {
		if (Str.isEmpty(style) && padding == null && margin == null) {
			return "";
		}

		StringBuffer sb = new StringBuffer();

		if (!Str.isEmpty(style)) {
			sb.append("border").append(_post).append(":");
			sb.append(width == null ? 0 : width).append("px").append(" ").append(style).append(" ").append(color).append(";");
		}

		if (padding != null)
			sb.append("padding").append(_post).append(":").append(padding).append("px;");
		if (margin != null)
			sb.append("margin").append(_post).append(":").append(margin).append("px;");

		return sb.toString();
	}

	public Integer getWidth() {
		return width;
	}

	public String getStyle() {
		return style;
	}

	public String getColor() {
		return color;
	}

	public Integer getPadding() {
		return padding;
	}

	public Integer getMargin() {
		return margin;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setPadding(Integer padding) {
		this.padding = padding;
	}

	public void setMargin(Integer margin) {
		this.margin = margin;
	}

}
