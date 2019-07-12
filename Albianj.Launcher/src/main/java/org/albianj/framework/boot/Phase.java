package org.albianj.framework.boot;

import org.albianj.framework.boot.tags.BundleSharingTag;

@BundleSharingTag
public enum Phase {
    Prepare(10, "[Code:10,Tag:PrepareForRuning]"),
    PrepareEnd(20, "[Code:20,Tag:PrepareEndThenWaitRunning]"),
    Run(30, "[Code:30,Tag:Running]"),
    RunEnd(40, "[Code:40,Tag:RunningStop]");

    private int st;
    private String desc;

    Phase(int st, String desc) {
        this.st = st;
        this.desc = desc;
    }

    public int getPhaseCode() {
        return this.st;
    }

    public String getDescription() {
        return this.desc;
    }
}
