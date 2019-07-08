package org.albianj.framework.boot.logging;


import org.albianj.framework.boot.tags.BundleSharingTag;

@BundleSharingTag
public interface ILogger {

    void log(String sessionId, String bundleName, LoggerLevel level, Class<?> calledClzz, Throwable e, String breif, String secretMsg, String msg);

    void openBufferAppender(String logName, String path, String maxFilesize);

    void openConsoleAppender();

    void closeConsoleAppender();

    boolean isConsoleAppenderOpened();

    void setLoggerLevel(String level);

    void setMaxFilesize(String maxFilesize);

}
