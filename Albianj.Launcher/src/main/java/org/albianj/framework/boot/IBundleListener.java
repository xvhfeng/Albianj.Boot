package org.albianj.framework.boot;

import org.albianj.framework.boot.tags.BundleSharingTag;

@BundleSharingTag
public interface IBundleListener {
    public void onActionExecute(BundleContext bctx);
}
