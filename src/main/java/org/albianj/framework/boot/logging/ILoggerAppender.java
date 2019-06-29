package org.albianj.framework.boot.logging;


import org.albianj.framework.boot.tags.BundleSharingTag;

@BundleSharingTag
public interface ILoggerAppender {
//    void open();

    void write(String src);

    void flush();

    void close();

    public String getMaxFilesize();

    public void setMaxFilesize(String sMaxFilesize) ;
}
