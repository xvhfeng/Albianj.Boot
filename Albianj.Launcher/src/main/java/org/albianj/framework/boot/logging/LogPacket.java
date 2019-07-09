package org.albianj.framework.boot.logging;

import org.albianj.framework.boot.servants.StringServant;

/**
 * 日志的数据结构
 * 类似代码使用如下：注意commit必须最后被调用
 * <code>
 *     LogServant.Instance.newLogPacket()
 *     .forSessionId("sessionId")
 * .atLevel(Level.Debug)
 * .inClass(refType)
 * .withCause(throw,false)
 * .addBrief("brief")
 * .addSecretMsg("wo get secret msg -> {0} in excp",e)
 * .addMsg("wo get exc {0} in excp",e)
 * .toLogger("loggerName");
 * </code>
 */
public class LogPacket {
    private long datetimeMS;
    private LoggerLevel level;
    private Class<?> refType;
    private Throwable cause;
    private boolean isKeepThrow = false;
    private String brief;
    private String secretMsg;
    private String logMsg;
    private String sessionId;

    public LogPacket(){
        this.datetimeMS = System.currentTimeMillis();
    }

    public LogPacket forSessionId(String sessionId){
        this.sessionId = sessionId;
        return this;
    }
    public LogPacket atLevel(LoggerLevel level){
        this.level = level;
        return this;
    }

    public LogPacket inClass(Class<?> refType){
        this.refType = refType;
        return this;
    }

    public LogPacket withCause(Throwable cause, boolean isKeepThrow){
        this.cause = cause;
        this.isKeepThrow = isKeepThrow;
        return this;
    }

    public LogPacket takeBrief(String fmt, Object... vals){
        this.brief = StringServant.Instance.format(fmt,vals);
        return this;
    }

    public LogPacket addSecretMsg(String fmt, Object... vals){
        this.secretMsg = StringServant.Instance.format(fmt,vals);
        return this;
    }

    public LogPacket addMsg(String fmt, Object... vals){
        this.logMsg = StringServant.Instance.format(fmt,vals);
        return this;
    }

    public void toLogger(String logName){
        LogMgr.Instance.addLogger(logName,this);
    }

    public LoggerLevel getLevel() {
        return level;
    }

    public String toString(){
        return null;
    }

}
