package org.albianj.framework.boot.logging;

import org.albianj.framework.boot.ApplicationContext;
import org.albianj.framework.boot.BundleContext;
import org.albianj.framework.boot.except.ThrowableServant;
import org.albianj.framework.boot.servants.StringServant;


public class LogPacketBuilder {
    private LoggerLevel level = LoggerLevel.Warn;
    private Class<?> refType;
    private Throwable cause;
    private boolean isThrow = false;
    private String brief;
    private String secretMsg;
    private String logMsg;
    private String sessionId;
    private String bundleName;
    private Thread refThread;

    public LogPacketBuilder(){
    }
    /**
     * 记录日志的操作者或者会话id
     * @param sessionId
     * @return
     */
    public LogPacketBuilder forSessionId(String sessionId){
       this.sessionId = sessionId;
        return this;
    }

    /**
     * 日志级别
     * @param level
     * @return
     */
    public LogPacketBuilder atLevel(LoggerLevel level){
        this.level = level;
        return this;
    }

    /**
     * 记录日志的发生地class
     * @param refType
     * @return
     */
    public LogPacketBuilder byCalled(Class<?> refType){
        this.refType = refType;
        return this;
    }

    /**
     * 日志记录发生的bundle名称
     * @param bundleName
     * @return
     */
    public LogPacketBuilder aroundBundle(String bundleName){
        this.bundleName = bundleName;
        return this;
    }

    /**
     * 当前的线程
     * @param currThread
     * @return
     */
    public LogPacketBuilder inThread(Thread currThread) {
        this.refThread = currThread;
        return this;
    }

    /**
     * 记录异常,并且是否继续抛出异常
     * @param cause
     * @return
     */
    public LogPacketBuilder withCause(Throwable cause){
        this.cause = cause;
        return this;
    }

    /**
     * 继续活着new一个异常抛出
     * 默认不会继续抛出异常
     * @param isThrow
     * @return
     */
    public LogPacketBuilder alwaysThrow(boolean isThrow){
        this.isThrow = isThrow;
        return this;
    }

    /**
     * 日志的简短说明
     * @param brief
     * @return
     */
    public LogPacketBuilder takeBrief(String brief){
        this.brief = brief;
        return this;
    }

    /**
     * 具有保密信息的日志内容
     * @param fmt
     * @param vals
     * @return
     */
    public LogPacketBuilder keepSecret(String fmt, Object... vals){
        this.secretMsg = StringServant.Instance.format(fmt,vals);
        return this;
    }

    /**
     * 日志的内容
     * @param fmt
     * @param vals
     * @return
     */
    public LogPacketBuilder addMessage(String fmt, Object... vals){
        this.logMsg = StringServant.Instance.format(fmt,vals);
        return this;
    }

    public LogPacket build(){
         if(StringServant.Instance.isNullOrEmptyOrAllSpace(sessionId)) {
             ThrowableServant.Instance.throwDisplayException(this.getClass(),
                     null,"Log Argument Error",
                     "Argument:sessionId -> [{0}]，but is must not be null,empty and all space.",
                     null == sessionId ? "NULL" : sessionId);
         }

        if(null == this.refThread) {
            this.refThread = Thread.currentThread();
        }
        if(null == this.refType ){
            this.refType = this.getClass();
        }

        String bName = "Application";
        if(StringServant.Instance.isNullOrEmptyOrAllSpace(bundleName)) {
            BundleContext ctx =  ApplicationContext.Instance.findCurrentBundleContext(this.getClass(),false);
            if(null != ctx){
                bName = ctx.getBundleName();
            }
        } else {
            bName = bundleName;
        }
        LogPacket packet = new LogPacket();
        packet.setRefThread(this.refThread);
        packet.setBrief(StringServant.Instance.isNullOrEmptyOrAllSpace(this.brief) ? "NULL" : this.brief);
        packet.setThrow(this.isThrow);
        packet.setCause(this.cause);
        packet.setBundle(bName);
        packet.setCalled(this.refType);
        packet.setLevel(this.level);
        packet.setMsg(StringServant.Instance.isNullOrEmptyOrAllSpace(this.logMsg) ? "NULL" : this.logMsg);
        packet.setSecret(StringServant.Instance.isNullOrEmptyOrAllSpace(this.secretMsg) ? "NULL" : this.secretMsg);
        packet.setSessionId(this.sessionId);
        return packet;
    }
}
