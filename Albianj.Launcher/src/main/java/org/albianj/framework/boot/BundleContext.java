package org.albianj.framework.boot;

import org.albianj.framework.boot.loader.AlbianClassLoader;
import org.albianj.framework.boot.loader.BundleClassLoader;
import org.albianj.framework.boot.logging.LoggerLevel;
import org.albianj.framework.boot.tags.BundleSharingTag;
import org.albianj.framework.boot.logging.LogServant;

import java.io.File;
import java.lang.reflect.Method;

/**
 * albianj bundle的上下文
 */
@BundleSharingTag
public class BundleContext {

    private String bundleName;
    private ClassLoader classLoader;
    private ThreadGroup threadGroup;
    private String startupType;

    private IBundleListener beginStartupEvent;
    private IBundleListener beginRunEvent;
    private boolean isInstallSpxFile = false;

    private String workPath;
    private String binFolder;
    private String classesFolder;
    private String libFolder;
    private String confFolder;
    private String appsFolder;

    public BundleContext setBundleName(String name){
        this.bundleName = name;
        return this;
    }

    public BundleContext setClassLoader(ClassLoader loader){
        this.classLoader = loader;
        return this;
    }

    public BundleContext setStartupTypeName(String  typeName){
        this.startupType = typeName;
        return this;
    }

    public BundleContext setWorkFolder(String workFolder){
        this.workPath = workFolder;
        return this;
    }

//    public BundleContext setLogger(ILogger runtimeLogger){
//        this.runtimeLogger = runtimeLogger;
//        return this;
//    }

    public BundleContext setBeginStartupEvent(IBundleListener beginStartupEvent){
        this.beginStartupEvent = beginStartupEvent;
        return this;
    }

    public BundleContext setBeginRunEvent(IBundleListener beginRunEvent){
        this.beginRunEvent = beginRunEvent;
        return this;
    }

    /**
     * 是否加载spx文件，对于v1.*的albianj，请设置该选项
     * @param enable
     * @return
     */
    public BundleContext setInstallSpxFile(boolean enable){
        this.isInstallSpxFile = enable;
        return this;
    }

    public BundleContext build(String sessionId){
        if(null == this.classLoader) {
            BundleClassLoader classLoader = isInstallSpxFile
                    ?  AlbianClassLoader.newInstance(bundleName)
                    : BundleClassLoader.newInstance(bundleName);

            this.classLoader = classLoader;
        }
        if (workPath.endsWith(File.separator)) {
            this.workPath = workPath;
        } else {
            this.workPath = workPath + File.separator;
        }
        threadGroup = new ThreadGroup(bundleName);
        threadGroup.setDaemon(true);
        this.binFolder = this.workPath + "bin" + File.separator;
        this.libFolder = this.workPath + "lib" + File.separator;
        this.classesFolder = this.workPath + "classes" + File.separator;
        this.confFolder = this.workPath + "conf" + File.separator;
        this.appsFolder = this.workPath + "apps" + File.separator;

        LogServant.Instance.newLogPacket()
                .forSessionId(sessionId)
                .atLevel(LoggerLevel.Info)
                .byCalled(this.getClass())
                .takeBrief("Bundle Runtime Settings")
                .addMessage("Application startup at bin folder -> {0},lib folder -> {1},classes folder -> {2},conf folder -> {3},apps folder -> {4}.",
                        this.binFolder,this,libFolder,this.classesFolder,this.confFolder,this.appsFolder)
                .toLogger();

        return this;
    }

    protected BundleContext() {

    }

    public static BundleContext newInstance() {
        return new BundleContext();
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public String getWorkFolder() {
        return this.workPath;
    }

    public String getBinFolder() {
        return workPath + "bin" + File.pathSeparator;
    }

    public String getLibFolder() {
        return workPath + "lib" + File.pathSeparator;
    }

    public String getClassesFolder() {
        return workPath + "classes" + File.pathSeparator;
    }

    public String getConfFolder() {
        return workPath + "conf" + File.pathSeparator;
    }

    public String getAppsFolder() {
        return workPath + "apps" + File.pathSeparator;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public String getStartupType(){
        return this.startupType;
    }

    public ThreadGroup getThreadGroup(){
        return this.threadGroup;
    }

    public BundleThread newThread(String name, Runnable func){
        return new BundleThread(this,name,func);
    }

//    public ILogger findLogger() {
//        return runtimeLogger;
//    }

    public String findConfigFile(String simpleFileName){
        return this.confFolder + simpleFileName;
    }

    public void startup(final String[] args){
        if(null != this.beginStartupEvent) {
            this.beginRunEvent.onActionExecute(this);
        }
        BundleThread thread = null;
        try {
            thread = newThread(this.bundleName, new Runnable() {
                @Override
                public void run() {
                    BundleContext ctx = ApplicationContext.Instance.findBundleContext(bundleName, true);
                    String startupTypeName = ctx.getStartupType();
                    try {
                        Class<?> clzz = ctx.getClassLoader().loadClass(startupTypeName);
                        Object launcher =  clzz.newInstance();
                        Method startup = null;
                        startup = clzz.getMethod("startup",String[].class);
                        if(null == startup){
                            LogServant.Instance.newLogPacket()
                                    .forSessionId("LaunchThread")
                                    .atLevel(LoggerLevel.Error)
                                    .byCalled(this.getClass())
                                    .alwaysThrow(true)
                                    .takeBrief("Bundle launcher Error")
                                    .addMessage("Bundle -> {0} startup by Class -> {1},but it without method startup(string[] args).Make sure the method is exist",
                                            bundleName, startupTypeName)
                                    .toLogger();
                        }
                        if(null != beginRunEvent) {
                            beginRunEvent.onActionExecute(ctx);
                        }
                        startup.invoke(launcher,args);
                        LogServant.Instance.newLogPacket()
                                .forSessionId("LaunchThread")
                                .atLevel(LoggerLevel.Info)
                                .byCalled(this.getClass())
                                .takeBrief("Bundle launcher")
                                .addMessage("Startup bundle -> {0} with class -> {1} success.",
                                        bundleName, startupTypeName)
                                .toLogger();
                    } catch (Exception e) {
                        LogServant.Instance.newLogPacket()
                                .forSessionId("LaunchThread")
                                .atLevel(LoggerLevel.Error)
                                .byCalled(this.getClass())
                                .withCause(e)
                                .alwaysThrow(true)
                                .takeBrief("Bundle launcher error")
                                .addMessage("Startup bundle -> {0} with class -> {1} is error.",
                                        bundleName, startupTypeName)
                                .toLogger();
                    }
                }
            });
        }catch (Exception e){
            LogServant.Instance.newLogPacket()
                    .forSessionId("LaunchThread")
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("Bundle launcher error")
                    .addMessage("Open bundle thread to startup bundle -> {0} is error.",
                            bundleName)
                    .toLogger();
            return;
        }
        if(null != thread) {
            LogServant.Instance.newLogPacket()
                    .forSessionId("LaunchThread")
                    .atLevel(LoggerLevel.Info)
                    .byCalled(this.getClass())
                    .takeBrief("Bundle launcher")
                    .addMessage("Startup thread of bundle -> {0}....", bundleName)
                    .toLogger();
            thread.start();
        }
    }

}
