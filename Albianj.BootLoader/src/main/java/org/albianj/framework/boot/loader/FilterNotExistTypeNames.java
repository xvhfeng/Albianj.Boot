package org.albianj.framework.boot.loader;

import java.util.HashSet;
import java.util.Set;

/**
 * 需要过滤的加载类的名称
 * 不知道为啥，这玩意不加载也不会出错，但是会报烦人的异常
 * 所以把它过滤掉
 *
 * 注意：过滤掉这些类的时候，请确保检查过jar，并且确保这些class确实不存在
 */
public class FilterNotExistTypeNames {
    private static Set<String> typenames;
    static {
        typenames = new HashSet<>();
        typenames.add("org.apache.log4j.LayoutCustomizer");
        typenames.add("com.mysql.jdbc.LocalizedErrorMessages");
        typenames.add("java.sql.JDBCType");
        typenames.add("org.apache.log4j.LayoutBeanInfo");
        typenames.add("org.apache.log4j.PatternLayoutBeanInfo");
        typenames.add("org.albianj.logger.impl.AlbianFileAppenderCustomizer");
        typenames.add("org.apache.log4j.FileAppenderCustomizer");
        typenames.add("org.apache.log4j.WriterAppenderCustomizer");
        typenames.add("org.apache.log4j.AppenderSkeletonCustomizer");
        typenames.add("java.lang.ObjectCustomizer");
        typenames.add("java.lang.ObjectBeanInfo");
        typenames.add("org.apache.log4j.WriterAppenderBeanInfo");
        typenames.add("org.apache.log4j.AppenderSkeletonBeanInfo");
        typenames.add("org.apache.log4j.FileAppenderBeanInfo");
        typenames.add("org.albianj.logger.impl.AlbianFileAppenderBeanInfo");
        typenames.add("org.apache.log4j.PatternLayoutCustomizer");
        typenames.add("org.albianj.logger.impl.AlbianRollingFileAppenderBeanInfo");
        typenames.add("org.apache.log4j.RollingFileAppenderBeanInfo");
        typenames.add("org.apache.log4j.RollingFileAppenderCustomizer");
        typenames.add("org.albianj.logger.impl.AlbianRollingFileAppenderCustomizer");
        typenames.add("com.mysql.jdbc.LocalizedErrorMessages_zh_CN");
        typenames.add("com.mysql.jdbc.LocalizedErrorMessages_zh_Hans");
        typenames.add("com.mysql.jdbc.LocalizedErrorMessages_zh_Hans_CN");
        typenames.add("com.mysql.jdbc.LocalizedErrorMessages_zh");
    }

    public static boolean isFilter(String name){
        return typenames.contains(name);
    }
}
