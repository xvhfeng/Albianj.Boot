package org.albianj.framework.boot.tags;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({
        ElementType.PARAMETER,
        ElementType.TYPE,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.LOCAL_VARIABLE,
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR
})
@BundleSharingTag
public @interface CommentsTag {
    /**
     * comments context
     *
     * @return String, not have default
     */
    String value();
}


