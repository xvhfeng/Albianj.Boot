package org.albianj.framework.boot.logging;


import org.albianj.framework.boot.tags.BundleSharingTag;

/**
 * 日志的实用类,所有的日志都由这个类在使用.
 * 当app启动的时候,程序会默认在logspath下建立Runtime日志,默认大小为10MB,级别为DEBUG.
 * 该日志会负责启动部分的日志记录.
 * 当app解析了boot.xml文件后,会解析到其中的logger配置节,该配置节可以重新设置Runtime日志的大小与级别,但是文件路径无法更改
 * 并且,该配置信息的级别会立即起作用,文件大小会对下一个日志文件起作用.
 *
 * Albian的日志只有buffer appender与console appender.
 * 一般情况下,开发环境下buffer append与console的appender全部打开,线上环境只打开buffer appender即可.
 *
 * 日志buffer appender的flush有2中模式,一种是当buffer缓冲池满的时候
 *
 */
@BundleSharingTag
public class LogServant {

    public final static String RuntimeLogNameDef = "Runtimes";
    public final static String RuntimeLogLevelDef="DEBUG";

    public static LogServant Instance = null;

    static {
        if(null == Instance) {
            Instance = new LogServant();
        }
    }

    protected LogServant() {

    }

    public void init(String logName,String logsFolder,String level,boolean isOpenConsole){
        LogManager.Instance.init(logName,logsFolder,level,isOpenConsole);
    }

    public LogPacketBuilder newLogPacketBuilder(){
        return new LogPacketBuilder();
    }

    public void repair(LoggerConf logAttr){
        LogManager.Instance.repair(logAttr);
    }
}