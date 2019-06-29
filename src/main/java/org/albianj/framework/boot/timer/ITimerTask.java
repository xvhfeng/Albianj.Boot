package org.albianj.framework.boot.timer;


import org.albianj.framework.boot.tags.BundleSharingTag;

@BundleSharingTag
public interface ITimerTask {
    void run(ITimeoutEntry ITimeoutEntry, String argv);
}
