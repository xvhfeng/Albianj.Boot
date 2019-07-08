package org.albianj.framework.boot;

public class MainApp {
    public static void main(String[] args){
          ApplicationContext.Instance.setAppStartupType(MainApp.class)
                                 .setWorkFolder("D:\\work\\github\\Albianj.Boot\\Albianj.Launcher.Test\\src\\main\\resources")
                                  .setLoggerAttr("D:\\work\\github\\Albianj.Boot\\Albianj.Launcher.Test\\logs",true)
//                                 .addBundle("bundleName","path/to/bundle/",BundleLauncher.class);
                   .run(args);
       }
}
