package org.albianj.framework.boot.logging;

import java.util.HashMap;
import java.util.Map;

public class LogMgr extends  Thread{

    public static LogMgr Instance = null;

    static {
        if(null == Instance) {
            Instance = new LogMgr();
        }
    }

    private boolean isStop = false;

    protected LogMgr() {
        logs = new HashMap<>();
        logsConf = new HashMap<>();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                close();
            }
        }));
        this.setDaemon(true);
        this.setName("LogMgr");
        this.start();
    }

    private Map<String,LogContext> logs = null;
    private Map<String,ILoggerConf> logsConf = null;

    public void addLogger(String logName,LogPacket packet){
        LogContext logCtx = null;
        if(!logs.containsKey(logName)){
            synchronized (logs){
                if(logs.containsKey(logName)) {
                    logCtx = logs.get(logName);
                } else {
                    ILoggerConf lc = logsConf.get(logName);
                    logCtx = new LogContext(lc);
                    logs.put(logName, logCtx);
                }
            }
        }
        logCtx =  logs.get(logName);
        logCtx.addLogPacket(packet);
    }

    public void putConf(ILoggerConf logAttr){
        logsConf.put(logAttr.getLoggerName(),logAttr);
    }

    public void close(){
        isStop = true;
            for (Map.Entry<String, LogContext> entry : logs.entrySet()) {
                try {
                    entry.getValue().close();
                } catch (Exception e) {

                }
            }
    }

    public void run(){
        while (!isStop) {
            for (Map.Entry<String, LogContext> entry : logs.entrySet()) {
                try {
                    entry.getValue().flush(false);
                } catch (Exception e) {

                }
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
        }
    }
}
