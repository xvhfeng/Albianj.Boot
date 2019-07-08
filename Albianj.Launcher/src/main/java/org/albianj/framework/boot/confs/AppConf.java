package org.albianj.framework.boot.confs;


import org.albianj.framework.boot.logging.ILoggerConf;

import java.util.Map;

public class AppConf {
    private String appName = "AlbianDefaultApp";
    private String machineId;
    private String machineKey = "wefet45y56gd&^%&$($$fbf943sf98^&*&*%$@%$34tksdjfvh823r2=sdfssdfsdp[sfshfwwefwffwe";
    private String runtimeLevel = "DEBUG";

    /**
     * 根logger的配置信息
     */
    private ILoggerConf rootLoggerAttr;

    private Map<String, ILoggerConf> loggerAttrs;

    private Map<String, BundleConf> bundles;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getMachineKey() {
        return machineKey;
    }

    public void setMachineKey(String machineKey) {
        this.machineKey = machineKey;
    }

    public String getRuntimeLevel() {
        return runtimeLevel;
    }

    public void setRuntimeLevel(String runtimeLevel) {
        this.runtimeLevel = runtimeLevel;
    }

    public Map<String, ILoggerConf> getLoggerAttrs() {
        return loggerAttrs;
    }

    public void setLoggerAttrs(Map<String, ILoggerConf> loggerAttrs) {
        this.loggerAttrs = loggerAttrs;
    }

    public ILoggerConf getRootLoggerAttr() {
        return rootLoggerAttr;
    }

    public void setRootLoggerAttr(ILoggerConf rootLoggerAttr) {
        this.rootLoggerAttr = rootLoggerAttr;
    }

    public Map<String, BundleConf> getBundlesConf() {
        return bundles;
    }

    public void setBundlesConf(Map<String, BundleConf> bundles) {
        this.bundles = bundles;
    }
}
