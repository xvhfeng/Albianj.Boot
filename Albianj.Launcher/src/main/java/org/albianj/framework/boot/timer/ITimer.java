package org.albianj.framework.boot.timer;


import org.albianj.framework.boot.tags.BundleSharingTag;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@BundleSharingTag
public interface ITimer {
    ITimeoutEntry addTimeout(ITimerTask task, long delay, TimeUnit unit, String argv);
    Set<ITimeoutEntry> stop();
}
