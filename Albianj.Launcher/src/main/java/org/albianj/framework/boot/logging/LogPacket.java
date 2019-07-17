package org.albianj.framework.boot.logging;

import org.albianj.framework.boot.except.DisplayException;
import org.albianj.framework.boot.except.HiddenException;
import org.albianj.framework.boot.except.ThrowableServant;
import org.albianj.framework.boot.servants.DailyServant;
import org.albianj.framework.boot.servants.StringServant;
import org.albianj.framework.boot.tags.BundleSharingTag;

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
@BundleSharingTag
public class LogPacket {
    private long datetimeMS;
    private LoggerLevel level;
    private Class<?> refType;
    private Throwable cause;
    private boolean isThrow = false;
    private String brief;
    private String secretMsg;
    private String logMsg;
    private String sessionId;
    private String bundleName;
    private Thread refThread;

    public LogPacket(){
        this.datetimeMS = System.currentTimeMillis();
    }

    /**
     * 记录日志的操作者或者会话id
     * @param sessionId
     * @return
     */
    public void setSessionId(String sessionId){
        this.sessionId = sessionId;
    }

    /**
     * 日志级别
     * @param level
     * @return
     */
    public void setLevel(LoggerLevel level){
        this.level = level;
    }

    /**
     * 记录日志的发生地class
     * @param refType
     * @return
     */
    public void setCalled(Class<?> refType){
        this.refType = refType;
    }

    /**
     * 日志记录发生的bundle名称
     * @param bundleName
     * @return
     */
    public void setBundle(String bundleName){
        this.bundleName = bundleName;
    }

    /**
     * 记录异常,并且是否继续抛出异常
     * @param cause
     * @return
     */
    public void setCause(Throwable cause){
        this.cause = cause;
    }

    /**
     * 继续活着new一个异常抛出
     * 默认不会继续抛出异常
     * @param isThrow
     * @return
     */
    public void setThrow(boolean isThrow){
        this.isThrow = isThrow;
    }

    /**
     * 日志的简短说明
     * @param brief
     * @return
     */
    public void setBrief(String brief){
        this.brief = brief;
    }

    /**
     * 具有保密信息的日志内容
     * @return
     */
    public void setSecret(String msg){
        this.secretMsg = msg;
    }

    /**
     * 日志的内容
     * @return
     */
    public void setMsg(String msg){
        this.logMsg = msg;
    }

    public void setRefThread(Thread refThread){
        this.refThread = refThread;
    }

    /**
     * 记录到logName的日志
     * @param logName
     */
    public void toLogger(String logName){
        LogManager.Instance.addLogger(logName,this);
        if(!isThrow) return;

        if(cause instanceof HiddenException) {
            throw (HiddenException) cause;
        }
        if(cause instanceof DisplayException) {
            throw (DisplayException) cause;
        }

        if(StringServant.Instance.isNotNullAndNotEmptyAndNotAllSpace(secretMsg)){
            ThrowableServant.Instance.throwHiddenException(refType,cause,secretMsg, brief, logMsg);
        }
        ThrowableServant.Instance.throwDisplayException(refType,cause, brief, logMsg);
    }

    /**
     * 记录到Runtimes的日志
     */
    public void toLogger(){
        toLogger(LogServant.RuntimeLogNameDef);
    }

    public LoggerLevel getLevel() {
        return level;
    }

    public Thread getRefThread(){
        return this.refThread;
    }

    public String toString(){

        Thread rt = null == this.refThread ? Thread.currentThread() : this.refThread;
        String msg = StringServant.Instance.format(
                "{0} [{1}] Session:[{2}] Bundle:[{3}] Thread:[{4},{5}] Brief:[{6}] Secret:[{7}] Msg:[{8}] {9}. {10}",
                DailyServant.Instance.datetimeLongStringWithMillis(this.datetimeMS),
                level.getTag(),sessionId,bundleName,
                rt.getId(),rt.getName(),
                brief,secretMsg,logMsg,
                ThrowableServant.Instance.excp2LogMsg(cause,refType),
                System.lineSeparator());

        return msg;
    }

}
