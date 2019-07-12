package org.albianj.framework.boot.logging;

import org.albianj.framework.boot.ApplicationContext;
import org.albianj.framework.boot.BundleContext;
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

    public LogPacket(){
        this.datetimeMS = System.currentTimeMillis();
    }

    /**
     * 记录日志的操作者或者会话id
     * @param sessionId
     * @return
     */
    public LogPacket forSessionId(String sessionId){
        this.sessionId = sessionId;
        return this;
    }

    /**
     * 日志级别
     * @param level
     * @return
     */
    public LogPacket atLevel(LoggerLevel level){
        this.level = level;
        return this;
    }

    /**
     * 记录日志的发生地class
     * @param refType
     * @return
     */
    public LogPacket byCalled(Class<?> refType){
        this.refType = refType;
        return this;
    }

    /**
     * 日志记录发生的bundle名称
     * @param bundleName
     * @return
     */
    public LogPacket inBundle(String bundleName){
        this.bundleName = bundleName;
        return this;
    }

    /**
     * 记录异常,并且是否继续抛出异常
     * @param cause
     * @return
     */
    public LogPacket withCause(Throwable cause){
        this.cause = cause;
        return this;
    }

    /**
     * 继续活着new一个异常抛出
     * 默认不会继续抛出异常
     * @param isThrow
     * @return
     */
    public LogPacket alwaysThrow(boolean isThrow){
        this.isThrow = isThrow;
        return this;
    }

    /**
     * 日志的简短说明
     * @param brief
     * @return
     */
    public LogPacket takeBrief(String brief){
        this.brief = brief;
        return this;
    }

    /**
     * 具有保密信息的日志内容
     * @param fmt
     * @param vals
     * @return
     */
    public LogPacket keepSecret(String fmt, Object... vals){
        this.secretMsg = StringServant.Instance.format(fmt,vals);
        return this;
    }

    /**
     * 日志的内容
     * @param fmt
     * @param vals
     * @return
     */
    public LogPacket addMessage(String fmt, Object... vals){
        this.logMsg = StringServant.Instance.format(fmt,vals);
        return this;
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

        if(!StringServant.Instance.isNullOrEmptyOrAllSpace(secretMsg)){
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

    public String toString(){
        String bName = "Application";
        if(StringServant.Instance.isNullOrEmptyOrAllSpace(bundleName)) {
            BundleContext ctx =  ApplicationContext.Instance.findCurrentBundleContext(this.getClass(),false);
            if(null != ctx){
                bName = ctx.getBundleName();
            }
        } else {
            bName = bundleName;
        }

        String msg = StringServant.Instance.format(
                "{0} [{1}] Session:[{2}] Bundle:[{3}] Thread:[{4}] Brief:[{5}] Secret:[{6}] Msg:[{7}] [{8}]. {9}",
                DailyServant.Instance.datetimeLongStringWithMillis(this.datetimeMS),
                level.getTag(),sessionId,bName,Thread.currentThread().getId(),
                brief,StringServant.Instance.isNullOrEmpty(secretMsg) ? "NULL" : secretMsg,logMsg,
                ThrowableServant.Instance.buildThrowBuffer(cause,refType),
                System.lineSeparator());

        return msg;
    }

}
