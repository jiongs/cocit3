package com.kmjsoft.cocit.orm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体操作注释：使用该注释将自动为CoC实体类对应的业务表生成相关的业务操作。如：添加、删除、修改、查询等。
 * 
 * @author jiongsoft
 * 
 */
@Target(value = {})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface CocAction {

	// public long id() default 0;

	public String name() default "";

	public String GUID() default "";

	public String desc() default "";

	public int SN() default 0;

	public int type() default 0;

	public String mode() default "";

	public String pluginName() default "";

	public Class plugin() default void.class;

	public String image() default "";

	public String logo() default "";

	public String pageTemplate() default "";

	public String targetUrl() default "";

	public String targetWindow() default "";

	public boolean disabled() default false;

	public String info() default "";

	public String error() default "";

	public String warn() default "";

	public String params() default "";

	public String jsonData() default "";
}
