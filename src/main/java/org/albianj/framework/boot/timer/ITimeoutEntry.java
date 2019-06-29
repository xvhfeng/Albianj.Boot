package org.albianj.framework.boot.timer;


import org.albianj.framework.boot.tags.BundleSharingTag;

@BundleSharingTag
public interface ITimeoutEntry {
    ITimer timer();
    ITimerTask task();
    boolean isExpired();
    boolean isCancelled();
    boolean cancel();
}
