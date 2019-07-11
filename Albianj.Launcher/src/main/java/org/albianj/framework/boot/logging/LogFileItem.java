package org.albianj.framework.boot.logging;

import org.albianj.framework.boot.servants.ConvertServant;
import org.albianj.framework.boot.servants.DailyServant;
import org.albianj.framework.boot.servants.StringServant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LogFileItem {
    private String logName;
    private String logFolder;
    private long maxFilesizeB;
    private long busyB;
    private FileOutputStream fos;

    public LogFileItem(String logName, String path, String maxFilesize) {
        if (!path.endsWith(File.separator)) {
            logFolder = path + File.separator;
        }
        this.maxFilesizeB = ConvertServant.Instance.toFileSize(maxFilesize, 10 * 1024 * 1024);
        this.busyB = 0;
        this.logName = logName;
        newLogFile(logName, path);
    }

    private void newLogFile(String logName, String path) {
        File p = new File(path);
        if (!p.exists()) {
            p.mkdirs();
        }
        String filename = StringServant.Instance.join(logFolder, logName, "-", DailyServant.Instance.datetimeLongStringWithMillisNoSep(), ".log");
        try {
            fos = new FileOutputStream(filename, true);
        } catch (FileNotFoundException e) {

        }
        this.busyB = 0;
    }

    public void write(String buffer) {
        byte[] bytes = StringServant.Instance.StringToBytes(buffer);
        try {
            fos.write(bytes);
            fos.flush();
            busyB += bytes.length;
            if (busyB >= maxFilesizeB) {
                close();
                newLogFile(this.logName,this.logFolder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.busyB = 0;
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void repair(String logFolder, String maxFilesize) {
        this.maxFilesizeB = ConvertServant.Instance.toFileSize(maxFilesize, 10 * 1024 * 1024);
        this.logFolder = logFolder;
    }
}
