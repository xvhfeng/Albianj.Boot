package org.albianj.framework.boot.logging;


import org.albianj.framework.boot.tags.BundleSharingTag;

@BundleSharingTag
public class LoggerConf {

    private String loggerName = null;
    private String path = null;
    private String level = "INFO";
    private boolean openConsole = false;
    private String maxFilesize = "10MB";

    public LoggerConf(String loggerName, String path, String level, boolean isOpenConsole, String maxFilesize){
        this.loggerName = loggerName;
        this.path = path;
        this.level = level;
        this.openConsole = isOpenConsole;
        this.maxFilesize = maxFilesize;
    }
    public String getLoggerName() {
        return this.loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean isOpenConsole() {
        return this.openConsole;
    }

    public void setOpenConsole(boolean openConsole) {
        this.openConsole = openConsole;
    }

    public String getMaxFilesize() {
        return this.maxFilesize;
    }

    public void setMaxFilesize(String maxFilesize) {
        this.maxFilesize = maxFilesize;
    }
}
