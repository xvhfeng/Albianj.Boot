package org.albianj.framework.boot;


import org.albianj.framework.boot.logging.LoggerLevel;
import org.albianj.framework.boot.logging.LogServant;
import org.albianj.framework.boot.tags.BundleSharingTag;

/**
 * bundle执行的thread
 */
@BundleSharingTag
public class BundleThread extends Thread {

    private BundleContext bundleContext;

    public BundleThread(BundleContext bundleContext, String name, Runnable func){
        super(bundleContext.getThreadGroup(),func);
        this.bundleContext = bundleContext;
        super.setContextClassLoader(bundleContext.getClassLoader());
        super.setName(name);
        super.setDaemon(true);
    }

    public BundleContext getCurrentBundleContext(){
        return this.bundleContext;
    }

    //need logging
    @Override
    public void start() {
        super.start();
    }

    // hold all exception in the thread
    // so every thread exit normal
    @Override
    public void run() {
        try {
            super.run();
        }catch (Throwable e){
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId("BundleThread")
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .aroundBundle(bundleContext.getBundleName())
                    .withCause(e)
                    .alwaysThrow(true)
                    .takeBrief("BundleThread executer error")
                    .addMessage("execute bundle -> {0} thread  is error,then exit thread...",
                            bundleContext.getBundleName())
                    .build().toLogger();

        }finally {
            return;
        }
    }
}
