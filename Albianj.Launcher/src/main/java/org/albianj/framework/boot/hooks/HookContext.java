package org.albianj.framework.boot.hooks;

import org.albianj.framework.boot.tags.BundleSharingTag;

@BundleSharingTag
public class HookContext {
    public static HookContext ctx;
    static {
        ctx = new HookContext();
    }
}


