package org.albianj.framework.boot.except;


import org.albianj.framework.boot.servants.StringServant;
import org.albianj.framework.boot.tags.BundleSharingTag;

/**
 * 对外的异常，可显示的异常,一般使用在抛出异常时需要抛出可视的信息
 * 该异常必须经过开发人员处理,开发人员可以直接抛出这个异常
 * 该异常以后将贯穿这个框架,作为整个框架的根异常(另外一个根异常是AlbianRuntimeException)
 */
@BundleSharingTag
public class DisplayException extends RuntimeException {
    protected Throwable innerThrow = null;
    protected String showMsg = null;
    protected String brief;
    protected boolean hasInterThrow = false;
    protected Class<?> refType = null;

    /**
     * 创建一个新异常
     *
     * @param brief : 简短的异常描述,通常可以包括异常的id,唯一性指标,业务/模块名称等等
     * @param showMsg   : 异常的详细信息,注意:不能包括敏感信息,包括但不限于密码,用户名,手机,身份证号,数据库信息等等
     */
    public DisplayException( Class<?> refType, String brief, String showMsg) {
        this(refType, null,brief,showMsg);
    }

    /**
     * 使用已经抛出的异常创建一个新异常
     *
     * @param brief  : 简短的异常描述,通常可以包括异常的id,唯一性指标,业务/模块名称等等
     * @param showMsg    : 异常的详细信息,注意:不能包括敏感信息,包括但不限于密码,用户名,手机,身份证号,数据库信息等等
     * @param innerThrow : 原先程序抛出的异常
     */
    public DisplayException(Class<?> refType, Throwable innerThrow, String brief, String showMsg) {
        this.brief = brief;
        this.showMsg = showMsg;
        if(null != innerThrow) {
            this.hasInterThrow = true;
            this.innerThrow = innerThrow;
        }
        this.refType = refType;
    }



    public String getShowMsg(){
        return this.showMsg;
    }

    /**
     * exception的简单message
     * @return
     */
    @Override
    public String toString() {
        return StringServant.Instance.format("Brief:[{0}],ShowMsg:[{1}]",
                brief,showMsg);
    }

    public String getMessage() {
        return toString();
    }

    /**
     * 异常的详细message
     * @return
     */
    public String getLocalizedMessage() {
        String innerThrowMsg = null == innerThrow ? "NULL" : ThrowableServant.Instance.printThrowStackTrace(innerThrow);
        String causeMsg = null == innerThrow ? "NULL" : ThrowableServant.Instance.findThrowCauseMsg(innerThrow);
        return StringServant.Instance.format("RefClass:[{0}],Brief:[{1}],ShowMsg:[{2}],Cause:[{3}],Throw:[{4}]",
                refType.getName(),brief,showMsg,causeMsg,innerThrowMsg);
    }

    public Throwable getInnerThrow(){
        return this.innerThrow;
    }

    public boolean hasInterThrow(){
        return this.hasInterThrow;
    }

    public String getBrief() {
        return brief;
    }
}
