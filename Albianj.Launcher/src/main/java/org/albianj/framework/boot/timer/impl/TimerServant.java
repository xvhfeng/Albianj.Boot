package org.albianj.framework.boot.timer.impl;


import org.albianj.framework.boot.timer.ITimeoutEntry;
import org.albianj.framework.boot.timer.ITimerTask;
import org.albianj.framework.boot.tags.BundleSharingTag;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@BundleSharingTag
public class TimerServant {
    public static TimerServant Instance = null;
    static {
        Instance = new TimerServant();

    }
    private TimerWheel wheel = new TimerWheel();

    public ITimeoutEntry addEntry(ITimerTask task, long delay, TimeUnit unit, String argv){
        return wheel.addTimeout(task,delay,unit,argv);
    }

    public void cancelEntry(ITimeoutEntry entry){
        if(!entry.isCancelled()) {
            entry.cancel();
        }
    }

    public Set<ITimeoutEntry> stop(){
        return wheel.stop();
    }

}
