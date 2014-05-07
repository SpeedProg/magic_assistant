package com.reflexit.magiccards.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An element annotated with NutNull claims <code>null</code> value is <em>forbidden</em> to return
 * (for methods), pass to (parameters) and hold (local variables and fields). Apart from
 * documentation purposes this annotation is intended to be used by static analysis tools to
 * validate against probable runtime errors and element contract violations.
 * 
 * @ConstraintValidator(NotNullConstraint.class)
 * @author max
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, })
public @interface NotNull {
	/**
	 * @return unknown
	 */
	String value() default "";

	/**
	 * @return the message if this field is null.
	 */
	String message() default "must not be null.";
}