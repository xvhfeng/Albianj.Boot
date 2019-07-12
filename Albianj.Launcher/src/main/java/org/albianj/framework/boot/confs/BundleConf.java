package org.albianj.framework.boot.confs;

import org.albianj.framework.boot.tags.BundleSharingTag;

@BundleSharingTag
public class BundleConf {
    private String name;
    private String workFolder;
    private String startupClassname;
    private boolean isInstallSpxFile = false;

    public BundleConf(String name, String workFolder, String startupClassname,boolean isInstallSpxFile) {
        this.name = name;
        this.workFolder = workFolder;
        this.startupClassname = startupClassname;
        this.isInstallSpxFile = isInstallSpxFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorkFolder() {
        return workFolder;
    }

    public void setWorkFolder(String workFolder) {
        this.workFolder = workFolder;
    }

    public String getStartupClassname() {
        return startupClassname;
    }

    public void setStartupClassname(String startupClassname) {
        this.startupClassname = startupClassname;
    }

    public boolean isInstallSpxFile() {
        return isInstallSpxFile;
    }

    public void setInstallSpxFile(boolean installSpxFile) {
        isInstallSpxFile = installSpxFile;
    }
}
