package org.albianj.framework.boot.loader;

import org.albianj.framework.boot.logging.LogServant;
import org.albianj.framework.boot.logging.LoggerLevel;
import org.albianj.framework.boot.tags.BundleSharingTag;
import sun.font.SunFontManager;

import java.io.File;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 所有class文件在jvm中的元信息
 */
@BundleSharingTag
public class TypeFileMetadata {
    /**
     * 文件内容
     */
    private byte[] fileContentBytes;
    /**
     * 完整带有命名空间的类名,带.class后缀名
     */
    private String fullClassName;
    /**
     * 完整带有命名空间的类名,不带.class后缀名
     */
    private String fullClassNameWithoutSuffix;
    /**
     * md5签名
     */
    private String md5;
    /**
     * class归属的parent名字
     * 可能为目录名或者是jar
     */
    private String parentFileName;

    /**
     * 归属是目录还是jar
     */
    private boolean isParentJar;

    /**
     * class加载的实体
     */
    private Class<?> clzz;
    /**
     * 带有.class后缀名和命名空间的完全文件名
     */
    private String fullFileName;

    /**
     * 该类来自于哪个文件夹，可能的有：bin，lib，classes与so（so只有albianj v1.0才具有)
     */
    private String fromFolder;

    /**
     * 后缀的名称，包括后缀的.
     */
    private String suffixName;

    private TypeFileOpt fileOpt;

    /**
     * @param fullFileName     带有.class后缀名和命名空间的完全文件名,文件系统格式
     * @param fileContentBytes 文件内容
     * @param parentFileName   class归属的parent名字 可能为目录名或者是jar
     * @param isParentJar      归属是目录还是jar
     * @return
     */
    public static TypeFileMetadata makeClassFileMetadata( TypeFileOpt fileOpt,
                                                          String suffixName,
                                                          String fullFileName,
                                                         byte[] fileContentBytes,
                                                         String parentFileName,
                                                         boolean isParentJar,
                                                         String fromFolder) {
        TypeFileMetadata cfm = new TypeFileMetadata();
        cfm.setFileContentBytes(fileContentBytes);
        cfm.setParentJar(isParentJar);
        cfm.setParentFileName(parentFileName);
        cfm.setSuffixName(suffixName);
        cfm.setFileOpt(fileOpt);
        String md5 = md5(fileContentBytes);
        cfm.setMd5(md5);
        cfm.fromFolder = fromFolder;
        cfm.setFullFileName(fullFileName);
        String ffn = fullFileName.replace("/", ".").replace(File.separator, ".");
        cfm.setFullClassName(ffn);

        if (fullFileName.endsWith(suffixName)) {
            cfm.setFullClassNameWithoutSuffix(ffn.substring(0, fullFileName.lastIndexOf(suffixName)));
        } else {
            cfm.setFullClassNameWithoutSuffix(ffn);
        }

        return cfm;
    }

    public static String md5(byte[] bytes) {
        byte[] secretBytes = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            secretBytes = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Have not MD5!");
        }
        String padStr = "00000000000000000000000000000000";
        String md5code = new BigInteger(1, secretBytes).toString(16);// 16进制数字
        int len = 32 - md5code.length();
        padStr = padStr.substring(0, len);
        md5code = padStr + md5code;
        return md5code;
    }

    public byte[] getFileContentBytes() {
        return fileContentBytes;
    }

    public void setFileContentBytes(byte[] fileContentBytes) {
        this.fileContentBytes = fileContentBytes;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getParentFileName() {
        return parentFileName;
    }

    public void setParentFileName(String parentFileName) {
        this.parentFileName = parentFileName;
    }

    public Class<?> getType() {
        return clzz;
    }

    public void setType(Class<?> clzz) {
        this.clzz = clzz;
    }

    public boolean isParentJar() {
        return isParentJar;
    }

    public void setParentJar(boolean parentJar) {
        isParentJar = parentJar;
    }

    public String getFullFileName() {
        return fullFileName;
    }

    public void setFullFileName(String fullFileName) {
        this.fullFileName = fullFileName;
    }

    public String getFullClassNameWithoutSuffix() {
        return fullClassNameWithoutSuffix;
    }

    public void setFullClassNameWithoutSuffix(String fullClassNameWithoutSuffix) {
        this.fullClassNameWithoutSuffix = fullClassNameWithoutSuffix;
    }

    public String getFromFolder() {
        return fromFolder;
    }

    public void setFromFolder(String from) {
        this.fromFolder = from;
    }

    public String mkKey(){
        return this.fullClassNameWithoutSuffix;
    }

    public String getSuffixName() {
        return suffixName;
    }

    public void setSuffixName(String suffixName) {
        this.suffixName = suffixName;
    }

    public TypeFileOpt getFileOpt() {
        return fileOpt;
    }

    public void setFileOpt(TypeFileOpt fileOpt) {
        this.fileOpt = fileOpt;
    }

    public URL makeFileURL(){
        StringBuffer sb = new StringBuffer();
        if(isParentJar) {
            sb.append("jar:file:/");
            sb.append(parentFileName.replace("\\","/"))
                    .append("!/").append(fullFileName);
        } else {
            sb.append("file:/");
            sb.append(parentFileName.replace("\\","/"))
                    .append("!/").append(fullFileName);
        }
        String url = sb.toString();
        LogServant.Instance.newLogPacketBuilder().addMessage(
                "make resurce url -> {0}",
                url)
                .atLevel(LoggerLevel.Debug)
                .forSessionId("LoadResource")
                .byCalled(this.getClass())
                .takeBrief("Make Resource URL")
                .build().toLogger();
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            LogServant.Instance.newLogPacketBuilder().addMessage(
                    "make resurce url -> {0} was throw excp.",
                    url)
                    .atLevel(LoggerLevel.Debug)
                    .forSessionId("LoadResource")
                    .byCalled(this.getClass())
                    .withCause(e)
                    .takeBrief("Make Resource URL")
                    .build().toLogger();

           return null;
        }
    }
}
