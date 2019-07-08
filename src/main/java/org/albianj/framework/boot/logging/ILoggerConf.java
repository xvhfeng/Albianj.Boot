package org.albianj.framework.boot.logging;


import org.albianj.framework.boot.tags.BundleSharingTag;

@BundleSharingTag
public interface ILoggerConf {
    String getLoggerName();

    void setLoggerName(String loggerName);

    String getPath();

    void setPath(String path);

    String getLevel();

    void setLevel(String level);

    boolean isOpenConsole();

    void setOpenConsole(boolean openConsole);

    String getMaxFilesize();

    void setMaxFilesize(String maxFilesize);
}

