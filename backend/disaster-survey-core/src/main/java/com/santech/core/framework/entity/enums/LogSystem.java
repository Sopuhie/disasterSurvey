package com.santech.core.framework.entity.enums;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogSystem {

	/**
	 * 操作对象
	 */
 	String target() default "";

 	/**
 	 * 操作类型
 	 */
	String type() default "";

}
