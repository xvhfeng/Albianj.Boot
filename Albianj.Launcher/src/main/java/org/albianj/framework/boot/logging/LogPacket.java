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
    private long  debugIdx = 0;
    private StackTraceElement rbp = null;

    public LogPacket(){
        this.datetimeMS = System.currentTimeMillis();
        StackTraceElement[] stacks =  Thread.currentThread().getStackTrace();
        if(stacks.length < 4) {
            rbp = stacks[stacks.length];
        } else {
            rbp = stacks[3];
        }
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

    public long getDebugIdx() {
        return debugIdx;
    }

    public void setDebugIdx(long debugIdx) {
        this.debugIdx = debugIdx;
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
                "{0} [{7}] Oper:{6} Bundle:{1} Thread:[{8},{9}] RBP:[{13}.{14}@{15}:{16}] Ref:{2} Brief:[{4}] Msg:{3} Secret:{5} {10}.{11}",
                DailyServant.Instance.datetimeLongStringWithMillis(this.datetimeMS),
                bundleName,refType.getName(),logMsg,brief,secretMsg,
                sessionId,level.getTag(), rt.getId(),rt.getName(),
                ThrowableServant.Instance.excp2LogMsg(cause,refType),
                System.lineSeparator(), this.debugIdx,
                rbp.getClassName(),rbp.getMethodName(),rbp.getFileName(),rbp.getLineNumber());

        return msg;
    }

}
