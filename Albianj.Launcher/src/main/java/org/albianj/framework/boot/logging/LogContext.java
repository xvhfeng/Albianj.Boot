package org.albianj.framework.boot.logging;

import java.util.ArrayList;

public class LogContext {
    private LogFileItem lfi;
    private ArrayList<LogPacket> aq = new ArrayList();
    private ArrayList<LogPacket> bq = new ArrayList();
    private ArrayList<LogPacket> cq = aq; // current queue
    private char cqName = 'A';
    private LoggerLevel level = LoggerLevel.All;
    private volatile boolean isStopAccept = false;
    private LoggerConf logConf;

    public LogContext(LoggerConf logConf) {
        this.logConf = logConf;
        this.level = LoggerLevel.toLevel(logConf.getLevel());
    }

    public void addLogPacket(LogPacket packet) {
        if (packet.getLevel().getLevel() < this.level.getLevel()) { //filter logger
            return;
        }

        synchronized (this) {
            if (isStopAccept) return;
            cq.add(packet);
        }
    }

    public void flush(boolean isStopAccept) {
        ArrayList<LogPacket> q = null;

        synchronized (this) {
            this.isStopAccept = isStopAccept;
            q = cq;
            if ('A' == cqName) { // current packet queue is A
                cq = bq;
                cqName = 'B';
            } else { // current packet queue is B
                cq = aq;
                cqName = 'A';
            }
        }

        if (q.isEmpty()) {
            return;
        }

        if (null == lfi) {
            lfi = new LogFileItem(logConf.getLoggerName(), logConf.getPath(), logConf.getMaxFilesize());
        }

        for (LogPacket p : q) {
            String buf = p.toString();
            lfi.write(buf);
            if (logConf.isOpenConsole()) {
                System.out.print(buf);
            }
        }
        q.clear();
        return;
    }

    public void close() {
        flush(true);
    }

    public void repaire(LoggerConf logConf) {
        this.logConf = logConf;
        this.level = LoggerLevel.toLevel(logConf.getLevel());
        if (null != lfi) {
            lfi.repair(logConf.getPath(), logConf.getMaxFilesize());
        }
    }
}
