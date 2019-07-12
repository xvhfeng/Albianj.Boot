package org.albianj.framework.boot;

import org.albianj.framework.boot.loader.AlbianClassLoader;
import org.albianj.framework.boot.loader.BundleClassLoader;
import org.albianj.framework.boot.logging.LoggerLevel;
import org.albianj.framework.boot.servants.StringServant;
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
    private String  startupClassname;

    private IBundleListener beginStartupEvent;
    private IBundleListener beginRunEvent;
    private boolean isInstallSpxFile = false;

    private String workPath;
    private String binFolder;
    private String classesFolder;
    private String libFolder;
    private String confFolder;
    private String appsFolder;
    private Phase phase;
    private String[] args;

    public BundleContext setBundleName(String name){
        this.bundleName = name;
        return this;
    }

    public BundleContext setClassLoader(ClassLoader loader){
        this.classLoader = loader;
        return this;
    }

    public BundleContext setStartupClassName(String  startupClassname){
        this.startupClassname = startupClassname;
        return this;
    }

    public BundleContext setWorkFolder(String workFolder){
        this.workPath = workFolder.endsWith(File.separator) ? workFolder : workFolder +File.separator;
        return this;
    }

    public BundleContext setBinFolder(String binFolder){
        this.binFolder = binFolder.endsWith(File.separator) ? binFolder : binFolder +File.separator;
        return this;
    }

    public BundleContext setLibFolder(String libFolder){
        this.libFolder = libFolder.endsWith(File.separator) ? libFolder : libFolder +File.separator;
        return this;
    }

    public BundleContext setClassesFolder(String classesFolder){
        this.classesFolder = classesFolder.endsWith(File.separator) ? classesFolder : classesFolder +File.separator;
        return this;
    }

    public BundleContext setConfFolder(String confFolder){
        this.confFolder = confFolder.endsWith(File.separator) ? confFolder : confFolder +File.separator;
        return this;
    }

    public BundleContext setAppsFolder(String appsFolder){
        this.appsFolder = appsFolder.endsWith(File.separator) ? appsFolder : appsFolder +File.separator;
        return this;
    }

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

    public BundleContext build(){
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


        this.phase = Phase.PrepareEnd;
        return this;
    }

    protected BundleContext() {
        this.phase = Phase.Prepare;
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
        return StringServant.Instance.isNullOrEmptyOrAllSpace(this.binFolder)
         ? workPath + "bin" + File.pathSeparator : this.binFolder;
    }

    public String getLibFolder() {
        return StringServant.Instance.isNullOrEmptyOrAllSpace(this.libFolder)
                ? workPath + "lib" + File.pathSeparator : this.libFolder;
    }

    public String getClassesFolder() {
        return StringServant.Instance.isNullOrEmptyOrAllSpace(this.classesFolder)
                ? workPath + "classes" + File.pathSeparator : this.classesFolder;
    }

    public String getConfFolder() {
        return StringServant.Instance.isNullOrEmptyOrAllSpace(this.confFolder)
                ? workPath + "config" + File.pathSeparator : this.confFolder;
    }

    public String getAppsFolder() {
        return StringServant.Instance.isNullOrEmptyOrAllSpace(this.appsFolder)
                ? workPath + "apps" + File.pathSeparator : this.appsFolder;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public String getStartupClassName(){
        return this.startupClassname;
    }

    public ThreadGroup getThreadGroup(){
        return this.threadGroup;
    }

    public BundleThread newThread(String name, Runnable func){
        return new BundleThread(this,name,func);
    }

    public String findConfigFile(String simpleFileName){
        return this.getConfFolder() + simpleFileName;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public String[] getArgs() {
        return args;
    }

    public BundleContext setArgs(String[] args) {
        this.args = args;
        return this;
    }

    public void startup(final String[] args){
        LogServant.Instance.newLogPacket()
                .forSessionId("StartThread")
                .atLevel(LoggerLevel.Info)
                .byCalled(this.getClass())
                .takeBrief("Bundle Runtime Settings")
                .addMessage("Application startup at bin folder -> {0},lib folder -> {1},classes folder -> {2},conf folder -> {3},apps folder -> {4}.",
                        this.binFolder,this,libFolder,this.classesFolder,this.confFolder,this.appsFolder)
                .toLogger();


        this.phase = Phase.Run;
        if(null != this.beginStartupEvent) {
            this.beginRunEvent.onActionExecute(this);
        }
        BundleThread thread = null;
        final BundleContext bctx = this;
        try {
            thread = newThread(this.bundleName, new Runnable() {
                @Override
                public void run() {
//                    BundleContext ctx = ApplicationContext.Instance.findBundleContext(bundleName, true);
                    String startupTypeName = bctx.getStartupClassName();
                    try {
                        Class<?> clzz = bctx.getClassLoader().loadClass(startupTypeName);
                        Object launcher =  clzz.newInstance();
                        Method startup = null;
                        startup = clzz.getMethod("startup");
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
                            beginRunEvent.onActionExecute(bctx);
                        }
                        startup.invoke(launcher);
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
                    .withCause(e)
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
