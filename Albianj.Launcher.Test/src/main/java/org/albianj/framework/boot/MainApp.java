package org.albianj.framework.boot;

public class MainApp {
    public static void main(String[] args) {
        ApplicationContext.Instance.setAppStartupType(MainApp.class)
                .setWorkFolder("D:\\work\\github\\Albianj.Boot\\Albianj.Launcher.Test\\src\\main\\resources")
                .setLoggerAttr("D:\\work\\github\\Albianj.Boot\\Albianj.Launcher.Test\\logs", true)
//                                 .addBundle("bundleName","path/to/bundle/",BundleLauncher.class);
                .run(args);
//        LogServant.Instance.init(null, null, null, false);
//
//        LogServant.Instance.repair(null);

//        LogServant.Instance.newLogPacket()
//                .forSessionId("sessionId")
//                .atLevel(LoggerLevel.Info)
//                .byCalled(MainApp.class)
//                .aroundBundle("Application")
//                .keepSecret("secret message")
//                .alwaysThrow(true)
//                .takeBrief("Main class Info")
//                .addMessage("we get {0}.", MainApp.class.getName())
//                .toLogger();
    }
}
