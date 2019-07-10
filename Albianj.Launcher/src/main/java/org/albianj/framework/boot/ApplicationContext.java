package org.albianj.framework.boot;


import org.albianj.framework.boot.confs.AppConf;
import org.albianj.framework.boot.confs.BundleConf;
import org.albianj.framework.boot.except.ThrowableServant;
import org.albianj.framework.boot.loader.BundleClassLoader;
import org.albianj.framework.boot.logging.LogServant;
import org.albianj.framework.boot.logging.LoggerLevel;
import org.albianj.framework.boot.logging.LoggerConf;
import org.albianj.framework.boot.servants.*;
import org.w3c.dom.Node;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 所有的albianj的应用程序主入口，管理albianj的进程
 * ApplicationContext 每个进程有且只能有一个。
 * 每个AlbianApplication中有一个root的bundle，使用bootContext来管理。
 * rootBundle是一个管理程序，主要管理各个bundle以及为各个bundle进行有限的功能性托底。
 * 还可能有n个bundle，使用bundle来管理。
 *
 * <pre>{@code
 * public class StartupClass {
 *      public static void main(String[] args){
 *          ApplicationContext.Instance.setAppStartupType(StartupClass.class)
 *                  .setWorkFolder("main/project/folder/")
 *                  .setLogger("path/to/logger/",true)
 *                  .addBundle("bundleName","path/to/bundle/",BundleLauncher.class);
 *                  .run(args);
 *      }
 * }
 * }</pre>
 */
public class ApplicationContext {

    public static ApplicationContext Instance;

    static {
        Instance = new ApplicationContext();
    }

    AppConf appConf;
    private Phase phase;
    private Class<?> appStartupType = null;
    private String workFolder = null;
    private String logsPath = null;
    private boolean isOpenConsole = false;
    private Map<String, BundleConf> attAttrs = new HashMap<>();
    private Map<String, BundleContext> bundleContextMap = new HashMap<>();
    private Thread currentThread ;
    private boolean isWindows;
//    private ILogger logger;

    protected ApplicationContext() {
        currentThread = Thread.currentThread();
        String system = System.getProperty("os.name");
        this.isWindows = system.toLowerCase().contains("windows");
        this.phase = Phase.Prepare;
    }

    public boolean isWindows() {
        return this.isWindows;
    }

    public Thread findRootBundleThread() {
        return this.currentThread;
    }

    public ApplicationContext setAppStartupType(Class<?> appStartupType) {
        this.appStartupType = appStartupType;
        return this;
    }

    public ApplicationContext setWorkFolder(String workFolder) {
        this.workFolder = workFolder;
        return this;
    }

    public ApplicationContext setLoggerAttr(String logsPath, boolean isOpenConsole) {
        this.logsPath = logsPath;
        this.isOpenConsole = isOpenConsole;
        return this;
    }

    public ApplicationContext addBundle(Class<?> refType, String name, String workFolder, Class<?> bundleType, boolean isInstallSpxFile) {
        if (this.phase != Phase.Prepare) {
            ThrowableServant.Instance.throwDisplayException(refType, null,
                    "Application Phase Error",
                    "App current phase is {0},it allow add the bundle only app is {1}.",
                    this.phase.getDescription(), Phase.Prepare.getDescription());
        }
        BundleConf bundleAttr = new BundleConf(name, workFolder, bundleType.getName(), isInstallSpxFile);
        attAttrs.put(name, bundleAttr);
        return this;
    }

//    public ApplicationContext setLogger(ILogger logger) {
//        this.logger = logger;
//        return this;
//    }
//
//    public ILogger findLogger() {
//        return this.logger;
//    }

    public void attachBundle(Class<?> refType, String name, String workFolder, String bundleTypeName, boolean isInstallSpxFile, String[] args) {
        if (this.phase != Phase.Run) {
            ThrowableServant.Instance.throwDisplayException(refType, null,
                    "Application Phase Error",
                    "App current phase is {0},it allow attach the bundle only app is {1}..",
                    this.phase.getDescription(), Phase.Run.getDescription());
        }

        synchronized (this) {
            if (isBundleExist(name)) {
                ThrowableServant.Instance.throwDisplayException(refType, null,
                        "Repeat startup bundle.",
                        "Bundle -> {0} was starting.", name);
            }
            BundleConf conf = new BundleConf(name, workFolder, bundleTypeName, isInstallSpxFile);
            attAttrs.put(name, conf);
            BundleContext bundleCtx = BundleContext.newInstance()
                    .setBundleName(conf.getName())
                    .setWorkFolder(conf.getWorkFolder())
                    .setStartupTypeName(conf.getStartupClassname())
                    .build(refType.getName());
            bundleContextMap.put(conf.getName(), bundleCtx);
            bundleCtx.startup(args);
        }
    }

    public boolean isBundleExist(String bundleName) {
        return bundleContextMap.containsKey(bundleName);
    }

    /**
     * 找到bundle所属的上下文
     *
     * @param bundleName
     * @param isThrowIfBundleNotExit
     * @return
     */
    public BundleContext findBundleContext(String bundleName, boolean isThrowIfBundleNotExit) {
        if (isThrowIfBundleNotExit && (!isBundleExist(bundleName))) {
            ThrowableServant.Instance.throwDisplayException(this.getClass(), null,
                    "Not Found Bundle", "Bundle -> {0} is not found.", bundleName);
        }
        return bundleContextMap.get(bundleName);
    }

    /**
     * 得到当前线程所属BundleContext
     *
     * @return
     */
    public BundleContext findCurrentBundleContext(Class<?> refType, boolean isThrowIfBundleNotExist) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (!BundleClassLoader.class.isAssignableFrom(loader.getClass())) {
            if(!isThrowIfBundleNotExist) {
                return null;
            } else {
                LogServant.Instance.newLogPacket()
                        .forSessionId("StartupThread")
                        .atLevel(LoggerLevel.Error)
                        .byCalled(refType)
                        .alwaysThrow(true)
                        .takeBrief("BundleContext Not Exist")
                        .addMessage("BundleContext cannot found by current Thread.Because classloader of currentThread is not BundleClassLoader or ChildClass of BundleClassLoader.")
                        .toLogger();
            }
        }
        String bundleName = null;
        bundleName = ((BundleClassLoader) loader).getBundleName();

        if (StringServant.Instance.isNotNullOrEmptyOrAllSpace(bundleName) && isThrowIfBundleNotExist) {
            LogServant.Instance.newLogPacket()
                    .forSessionId("StartupThread")
                    .atLevel(LoggerLevel.Error)
                    .byCalled(refType)
                    .alwaysThrow(true)
                    .takeBrief("BundleContext Not Exist")
                    .addMessage("Current BundleContext name is null or empty or allSpace.")
                    .toLogger();
        }

        return findBundleContext(bundleName, isThrowIfBundleNotExist);
    }

    public void run(String[] args) {
        try {
            this.phase = Phase.PrepareEnd;
            buildApplicationRuntime(args);
            this.phase = Phase.Run;
            for(int i = 0; i <200000; i++){
                LogServant.Instance.newLogPacket()
                        .forSessionId("StartupThread")
                        .atLevel(LoggerLevel.Info)
                        .byCalled(this.getClass())
                        .takeBrief("Albianj Application Startup")
                        .addMessage("{2} Albianj application startuping by using RuntimeLogger with folder -> {0} and {1} open ConsoleLogger.",
                                logsPath, isOpenConsole ? "" : "not",i)
                        .toLogger();
            }
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            try {
                Thread.sleep(1000); //wait io flush
            } catch (InterruptedException e1) {

            }
            return;
        }
    }

    private boolean buildApplicationRuntime(String[] args) {
        if (StringServant.Instance.isNullOrEmptyOrAllSpace(logsPath)) {
            ThrowableServant.Instance.throwDisplayException(this.getClass(), null,
                    "Startup Argument Error.",
                    "Argument 'logsPath' is not setting and OPS(Yes,Have and only have caicai.) not allow use default value,so must setting it.");
        }

        /**
         * first init initLogger，but named Runtime and replace it when init end.
         */
        LogServant.Instance.init(LogServant.RuntimeLogNameDef, logsPath, LogServant.RuntimeLogLevelDef, isOpenConsole);

        LogServant.Instance.newLogPacket()
                .forSessionId("StartupThread")
                .atLevel(LoggerLevel.Info)
                .byCalled(this.getClass())
                .takeBrief("Albianj Application Startup")
                .addMessage("Albianj application startuping by using RuntimeLogger with folder -> {0} and {1} open ConsoleLogger.",
                        logsPath, isOpenConsole ? "" : "not")
                .toLogger();


        if (StringServant.Instance.isNullOrEmptyOrAllSpace(workFolder)) {
            if (null == this.appStartupType) {
                LogServant.Instance.newLogPacket()
                        .forSessionId("StartupThread")
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("Run Argument Error")
                        .addMessage("Application's workfolder or Class of main() function must set one.And we recommend set workfolder.")
                        .toLogger();
            }

            String workFolder = TypeServant.Instance.classResourcePathToFileSystemWorkFolder(this.appStartupType);
            if (!FileServant.Instance.isFileOrPathExist(workFolder)) {
                LogServant.Instance.newLogPacket()
                        .forSessionId("StartupThread")
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("Run Argument Error")
                        .addMessage("Application's workfolder is not exist,then workpath -> {0} is setting by class -> {1}.",
                                workFolder, this.appStartupType.getName())
                        .toLogger();
            }
            this.workFolder = workFolder;
            LogServant.Instance.newLogPacket()
                    .forSessionId("StartupThread")
                    .atLevel(LoggerLevel.Info)
                    .byCalled(this.getClass())
                    .takeBrief("Runtime Argument Defaulter")
                    .addMessage("Setting workFolder to -> {0} by class -> {1} default.",
                            workFolder, this.appStartupType.getName())
                    .toLogger();
        } else {
            LogServant.Instance.newLogPacket()
                    .forSessionId("StartupThread")
                    .atLevel(LoggerLevel.Info)
                    .byCalled(this.getClass())
                    .takeBrief("Runtime Settings")
                    .addMessage("Application startup at workFolder -> {0}.", workFolder)
                    .toLogger();
        }

        this.workFolder = FileServant.Instance.makeFolderWithSuffixSep(this.workFolder);

        /**
         * special deal app org.albianj.framework.test.org.albianj.framework.boot
         */
        repair("StartupThread", logsPath, attAttrs);

        for (Map.Entry<String, BundleConf> entry : attAttrs.entrySet()) {
            BundleConf conf = entry.getValue();
            BundleContext bundleCtx = BundleContext.newInstance()
                    .setBundleName(conf.getName())
                    .setWorkFolder(conf.getWorkFolder())
                    .setStartupTypeName(conf.getStartupClassname())
                    .build("StartupThread");
            bundleContextMap.put(conf.getName(), bundleCtx);
            bundleCtx.startup(args);
        }
        return true;
    }

    public void exitSystem(int st) {
        try {
            Thread.sleep(5000); //wait io flush
            currentThread.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(st);
    }

    private XmlParserContext loadAppConf(String sessionId, String confFolder) {
        String bootXmlFilePath = findConfigFile(confFolder, "application.xml");
        XmlParserContext xmlParserCtx = XmlParserContext.makeXmlParserContext(sessionId, bootXmlFilePath).load();
        return xmlParserCtx;
    }

    private String findConfigFile(String workFolder, String simpleFileName) {
        return workFolder + "conf" + File.separator + simpleFileName;
    }

    private AppConf parserAppBundleConf(XmlParserContext xmlParserCtx, String logsPath) {
        AppConf appConf = new AppConf();
        String appName = xmlParserCtx.findNodeValue("Application/AppName", false, true);
        String mid = xmlParserCtx.findNodeValue("Application/MachineId", true, false);
        String mkey = xmlParserCtx.findNodeValue("Application/MachineKey", true, false);
        String runtimeLevel = xmlParserCtx.findNodeValue("Application/RuntimeLevel", true, false);
        if (StringServant.Instance.isNotNullOrEmptyOrAllSpace(runtimeLevel)) {
            appConf.setRuntimeLevel(runtimeLevel);
        }
        if (StringServant.Instance.isNotNullOrEmptyOrAllSpace(appName)) {
            appConf.setAppName(appName);
        }
        if (StringServant.Instance.isNotNullOrEmptyOrAllSpace(mkey)) {
            appConf.setMachineKey(mkey);
        }
        if (StringServant.Instance.isNotNullOrEmptyOrAllSpace(mid)) {
            appConf.setMachineId(mid);
        }


        Node loggersNode = xmlParserCtx.selectNode("Application/Logger", false);
        if (null == loggersNode) {
            LoggerConf logAttr = new LoggerConf("Runtimes", logsPath, "DEBUG", true, "10MB");
            appConf.setRootLoggerAttr(logAttr);
        } else {
            LoggerConf logAttr = parserAppRuntimeLoggerConf(xmlParserCtx, loggersNode, logsPath);
            appConf.setRootLoggerAttr(logAttr);
//            Map<String,ILoggerConf> logsAttr = parserLoggersConf(xmlParserCtx,loggersNode,logsPath);
//            bootAttr.setLoggerAttrs(logsAttr);
        }
        Map<String, BundleConf> bundles = parserChildBundlesConf(xmlParserCtx);
        if (!CollectServant.Instance.isNullOrEmpty(bundles)) {
            appConf.setBundlesConf(bundles);
        }
        return appConf;
    }

    private LoggerConf parserAppRuntimeLoggerConf(XmlParserContext xmlParserCtx, Node logNode, String logsPath) {
        String rootLevel = xmlParserCtx.findAttributeValue(logNode, "Level", "INFO");
        String rootConsole = xmlParserCtx.findAttributeValue(logNode, "Console", "true");
        String rootMaxFilesize = xmlParserCtx.findAttributeValue(logNode, "MaxFilesize", "10MB");
        LoggerConf logAttr = new LoggerConf("Runtime", logsPath, rootLevel,
                ConvertServant.Instance.toBoolean(rootConsole, true), rootMaxFilesize);
        return logAttr;
    }

    private Map<String, BundleConf> parserChildBundlesConf(XmlParserContext xmlParserCtx) {
        Map<String, BundleConf> bundlesAttr = new HashMap<>();
        List<Node> bundles = xmlParserCtx.selectNodes("Application/Bundles/Bundle", false);
        if (null == bundles || 0 == bundles.size()) {
            return bundlesAttr;
        }
        /**
         * key -> bundleName
         * value -> bundleAttr
         */
        for (Node n : bundles) {
            BundleConf bundleAttr = parserChildBundlesConf(xmlParserCtx, n);
            bundlesAttr.put(bundleAttr.getName(), bundleAttr);
        }
        return bundlesAttr;
    }

    private BundleConf parserChildBundlesConf(XmlParserContext xmlParserCtx, Node bundleNode) {
        String name = xmlParserCtx.findAttributeValue(bundleNode, "Name", false, true);
        String workPath = xmlParserCtx.findAttributeValue(bundleNode, "WorkPath", false, true);
        String startup = xmlParserCtx.findAttributeValue(bundleNode, "Startup", false, true);
        String installSpxFile = xmlParserCtx.findAttributeValue(bundleNode, "InstallSpxFile", true, false);
        boolean isInstallSpxFile = false;
        if (StringServant.Instance.isNotNullOrEmptyOrAllSpace(installSpxFile)) {
            try {
                isInstallSpxFile = Boolean.valueOf(installSpxFile);
            } catch (Exception e) {
                isInstallSpxFile = false;
            }
        }

        BundleConf bundleAttr = new BundleConf(name, workPath, startup, isInstallSpxFile);
        return bundleAttr;
    }

    private void repair(String sessionId, String logsPath, Map<String, BundleConf> attAttrs) {
        XmlParserContext confCtx = loadAppConf(sessionId, this.workFolder);
        appConf = parserAppBundleConf(confCtx, logsPath);
        LoggerConf logAttr = appConf.getRootLoggerAttr();
        LogServant.Instance.repair(logAttr);
        Map<String, BundleConf> confBundlesAttr = appConf.getBundlesConf();
        if (CollectServant.Instance.isNullOrEmpty(confBundlesAttr)) {
            attAttrs.putAll(confBundlesAttr);
        }
    }

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

}
