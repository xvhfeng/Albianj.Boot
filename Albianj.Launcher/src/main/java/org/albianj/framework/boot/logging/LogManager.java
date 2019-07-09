package org.albianj.framework.boot.logging;

import java.util.HashMap;
import java.util.Map;

public class LogManager extends Thread {

    public static LogManager Instance = null;

    static {
        if (null == Instance) {
            Instance = new LogManager();
        }
    }

    private boolean isStop = false;
    private Map<String, LogContext> logs = null;
    private Map<String, LoggerConf> logsConf = null;
    protected LogManager() {
        logs = new HashMap<>();
        logsConf = new HashMap<>();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                close();
            }
        }));
        this.setDaemon(true);
        this.setName("LogManager");
        this.start();
    }

    public void init(String logName,String logFolder,String level,boolean isOpenConsole){
        LoggerConf conf = new LoggerConf(logName,logFolder,level,isOpenConsole,"10MB");
        this.logsConf.put(LogServant.RuntimeLogNameDef,conf);
    }

    public void addLogger(String logName, LogPacket packet) {
        LogContext logCtx = null;
        if (!logs.containsKey(logName)) {
            synchronized (logs) {
                if (logs.containsKey(logName)) {
                    logCtx = logs.get(logName);
                } else {
                    LoggerConf conf = logsConf.get(logName);
                    logCtx = new LogContext(conf);
                    logs.put(logName, logCtx);
                }
            }
        }
        logCtx = logs.get(logName);
        logCtx.addLogPacket(packet);
    }

    public void repair(LoggerConf logAttr) {
        logsConf.put(logAttr.getLoggerName(), logAttr);
        if(logs.containsKey(logAttr.getLoggerName())) {
            logs.get(logAttr.getLoggerName()).repaire(logAttr);
        }
    }

    public void close() {
        isStop = true;
        for (Map.Entry<String, LogContext> entry : logs.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Exception e) {

            }
        }
    }

    public void run() {
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
