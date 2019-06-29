package org.albianj.framework.boot.logging.impl;


import org.albianj.framework.boot.except.LocationInfo;
import org.albianj.framework.boot.except.ThrowableServant;
import org.albianj.framework.boot.helpers.DailyServant;
import org.albianj.framework.boot.helpers.StringServant;
import org.albianj.framework.boot.logging.LoggerLevel;
import org.albianj.framework.boot.tags.BundleSharingTag;

/**
 * 日志的记录格式
 * Time Level SessionId BundleName ThreadID Brief Msg { CallChain InnerException[CauseChain,CallChain] }
 * CallChain :
 *  Classname.MethodName(Filename:Line) >> Classname.MethodName(Filename:Line) >> Classname.MethodName(Filename:Line) ...
 *
 * CauseChain : CauseType:CauseMsg >> CauseType:CauseMsg >> CauseType:CauseMsg ...
 *
 */
@BundleSharingTag
public class LayoutServant {
    public static LayoutServant Instance = null;

    static {
        if(null == Instance) {
            Instance = new LayoutServant();
        }
    }

    protected LayoutServant() {

    }

    public String makeLayoutBuffer(String sessionId, String bundleName, LoggerLevel level, LocationInfo localInfo, String breif, String secretMsg, String msg){
        StringBuilder sb = new StringBuilder();
        sb.append(DailyServant.Instance.datetimeLongString()).append(" ").append(level.getTag())
                .append(" Session:").append(sessionId)
                .append(" Bundle:").append(bundleName)
                .append(" Thread:").append(Thread.currentThread().getId())
                .append(" Breif:").append(breif)
                .append(" Secert:").append(StringServant.Instance.isNullOrEmptyOrAllSpace(secretMsg) ? "" : secretMsg)
                .append(" Msg:").append(msg);

        StringBuilder excMsg = ThrowableServant.Instance.buildThrowBuffer(localInfo.getThrowable(),localInfo.getRefType());
        sb.append(excMsg);
        return sb.toString();
    }
}
