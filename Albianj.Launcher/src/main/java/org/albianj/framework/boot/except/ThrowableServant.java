package org.albianj.framework.boot.except;


import org.albianj.framework.boot.servants.StringServant;
import org.albianj.framework.boot.tags.BundleSharingTag;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Stack;

@BundleSharingTag
public class ThrowableServant {

    public static ThrowableServant Instance = null;

    static {
        if(null == Instance) {
            Instance = new ThrowableServant();
        }
    }

    protected ThrowableServant() {

    }

    /**
     * 分析出现异常的真正原因。一直分析到root为止。
     * 注意：返回的buffer可能具有私密性，故只能作为log日志使用，绝对不能throw出去
     * @param e
     * @return
     */
    public String makeCauseChainBuffer(Throwable e){
        if(null == e) {
            return "NULL";
        }
        Throwable ptr = null;
        Throwable rc = e;
        Stack<Throwable> causeStack = new Stack<>();
        causeStack.push(rc); // push first interThrow
        while(null != (ptr = rc.getCause())  && (rc != ptr) ) {
            causeStack.push(ptr); // push interThrow chain
            rc = ptr;
        }

        StringBuilder sb = new StringBuilder();
        for(Throwable t : causeStack){
           sb.append(t.getClass().getSimpleName()).append(":").append(t.getMessage()).append(" >> ");
        }
        int len = sb.length();
        if(0 != len){
            sb.delete(len -4,len);
            return sb.toString();
        }
        return "NULL";
    }

    /**
     * 组合异常/指定发生异常时的堆栈信息
     * 发生异常的调用点为clzz参数指定
     * 当内部有异常时，clzz应为null，这样
     * @param e
     * @param refType
     * @return
     */
    public String makeStackChainBuffer(Throwable e,Class<?> refType){
        String callClassname = refType.getName();
        if(null == e){
            return StringServant.Instance.format("CallClass:[{0}] Stack:[NULL]",callClassname);
        }

        StackTraceElement[] stes = e.getStackTrace();
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement ste : stes){
            if ((null != refType) && !ste.getClassName().equals(callClassname)) {
                continue;
            }
            sb.append(ste.getClassName()).append(".").append(ste.getMethodName())
                    .append("(")
                    .append(ste.getFileName()).append(":").append(ste.getLineNumber())
                    .append(") -> ");
        }
        int len = sb.length();
        if(0 != len){
            sb.delete(len -4,len);
            return sb.toString();
        }
        return StringServant.Instance.format("CallClass:[{0}] Stack:[NULL]",callClassname);
    }

    public String throw2Buffer(Throwable e, Class<?> refType){
        String calledClassname = refType.getName();
        if(null == e) {
            return StringServant.Instance.format("CalledClass:[{0}] Throwable:[NULL]",calledClassname);
        }
        Throwable t = null;
        String throwBuffer = null;
        do {
            if (e instanceof HiddenException) {
                HiddenException he = (HiddenException) e;
                throwBuffer = StringServant.Instance.format(
                        "HiddenException -> CallClass:[{0}] [Brief:[{1}] HideMsg:[{2}] Msg:[{3}] Stack:[{4}]",
                        calledClassname,he.getBrief(),he.getHideMsg(),he.getShowMsg(),makeStackChainBuffer(he, refType));

                if(he.hasInterThrow()){
                    t = he.getInterThrow();
                }
                break;
            }
            if (e instanceof DisplayException) {
                DisplayException de = (DisplayException) e;
                throwBuffer = StringServant.Instance.format(
                        "DisplayException -> CallClass:[{0}] [Brief:[{1}] HideMsg:[{2}] Msg:[{3}] Stack:[{4}]",
                        calledClassname,de.getBrief(),"NULL",de.getShowMsg(),makeStackChainBuffer(de, refType));
                if(de.hasInterThrow()){
                    t = de.getInterThrow();
                }
                break;
            }

            if(e instanceof Throwable) {
                throwBuffer = StringServant.Instance.format(
                        "SystemException -> CallClass:[{0}] Msg:[{1}] Cause:[{2}] Stack:[{3}]",
                        calledClassname,e.getMessage(),makeCauseChainBuffer(e),makeStackChainBuffer(e, refType));
            }
        }while(false);

        /**
         * has inner exception
         *
         */
        String innerThrowBuffer = null;
        if(null != t) {
            /**
             * not to recursion deal the throwable
             * because HideException or DisplayException must be throw once
             */
            innerThrowBuffer = StringServant.Instance.format(
                    "CallClass:[{0}] Msg:[{1}] Cause:[{2}] Stack:[{3}]",
                    calledClassname,t.getMessage(),makeCauseChainBuffer(t),makeStackChainBuffer(t, refType));
        }
        return StringServant.Instance.format("Throw:[{0}] InnerThrow:[{1}]",
                throwBuffer,innerThrowBuffer);
    }

    /**
     * 将异常的堆栈信息转为字符串
     * @param e 异常
     * @return 异常的字符串描述
     */
    public String printThrowStackTrace(Throwable e) {
        if(null == e) {
            return "NULL";
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(bo);
        e.printStackTrace(pw);
        pw.flush();
        pw.close();
        return bo.toString();
    }

    /**
     * 将异常的堆栈信息转为行式的字符串
     * @param e 异常
     * @return 异常的字符串描述
     */
    public String printThrowStackTraceForLineStyle(Throwable e) {
        if(null == e) {
            return "NULL";
        }
        String line = printThrowStackTrace(e);
        return line.replace("\r\n"," >> ")
                .replace("\n"," >> ");
    }

    public void throwDisplayException(Class<?> refType, Throwable interThrow, String brief, String fmt, Object...obj) {
        String msg = StringServant.Instance.format(fmt,obj);
        if(null == interThrow){
            throw new DisplayException(refType,brief,msg);
        }
        throw new DisplayException(refType,interThrow,brief,msg);
    }

    public void throwHiddenException(Class<?> refType, Throwable interThrow, String brief, String hideMsg, String fmt, Object... obj) {
        String msg = StringServant.Instance.format(fmt,obj);
        if(null == interThrow){
            throw new HiddenException(refType,brief,hideMsg,msg);
        }
        throw new HiddenException(refType,interThrow,brief,hideMsg,msg);
    }

}
