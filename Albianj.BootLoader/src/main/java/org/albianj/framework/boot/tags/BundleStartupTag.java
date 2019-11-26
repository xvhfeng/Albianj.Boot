package org.albianj.framework.boot.tags;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface BundleStartupTag {
    String BundleName() default "";
}
