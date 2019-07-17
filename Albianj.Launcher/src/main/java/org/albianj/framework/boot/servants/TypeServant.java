package org.albianj.framework.boot.servants;


import org.albianj.framework.boot.tags.BundleSharingTag;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarInputStream;

@BundleSharingTag
public class TypeServant {
    public static TypeServant Instance = null;

    static {
        if(null == Instance) {
            Instance = new TypeServant();
        }
    }

    protected TypeServant() {

    }

    /**
     * 包括包名的class名转换成文件系统的文件名，如果后缀没有.class,会加后缀
     * @param fullClassName
     * @return
     */
    public  String fullClassNameToClassFileName(String fullClassName){
        String fsFilename =  fullClassName.replace('.', File.separatorChar) ;
        if(!fsFilename.endsWith(".class")) {
            return fsFilename +".class";
        }
        return fsFilename;
    }

    public String fullClassNameToClassFullFileName(String rootFolder,String fullClassName){
        if(rootFolder.endsWith(File.separator)) {
            return rootFolder + fullClassNameToClassFileName(fullClassName);
        }
        return  rootFolder + File.separator + fullClassNameToClassFileName(fullClassName);
    }

    public byte[] getClassFileBytes(String rootFolder,String fullClassName){
        String classFileName = fullClassNameToClassFullFileName(rootFolder,fullClassName);
        if(!FileServant.Instance.isFileOrPathExist(classFileName)) {
            return null;
        }
        return FileServant.Instance.getFileBytes(classFileName);
    }

    public String fullClassNameToSimpleClassName(String fullClassName){
        String name = fullClassName;
        if(name.endsWith(".class")){
            name = name.substring(0,name.lastIndexOf(".class") - 1);
        }
        return name.substring(name.lastIndexOf(".") + 1);
    }

    public String getSimpleClassName(Class<?> clzz){
        String name = clzz.getSimpleName();
        return name;
    }

    public byte[] readJarBytes(JarInputStream jis) throws IOException {
        int len = 0;
        byte[] bytes = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        while ((len = jis.read(bytes, 0, bytes.length)) != -1) {
            baos.write(bytes, 0, len);
        }
        return baos.toByteArray();
    }

    /**
     * 从给定的类（一般为带有Main函数的类）来获取程序的运行时项目工程路径
     * @param clzz 带有main函数的启动类
     * @return
     */
    public String  classResourcePathToFileSystemWorkFolder(Class<?> clzz){
        //from jar : jar:file:/mnt/d/tmp/clTest/out/artifacts/unnamed/unnamed.jar!/org/cltest/
        //from class file:/D:/tmp/clTest/out/production/clTest/org/cltest/
        String url = clzz.getResource("").toString();
        String path = null;
        if(url.startsWith("jar:")) {
            int jarSepIdx = url.lastIndexOf("!");
            path = url.substring(10,jarSepIdx);//jar proto must with file proto
            path = path.substring(0, path.lastIndexOf("/",path.lastIndexOf(".")-1));
            return path.replace("/",File.separator);
        }
        //file proto
        String pkgName = clzz.getPackage().getName();
        pkgName = pkgName.replace(".","/");
        // maybe in the linux,substring begin index is 5
        path = url.substring(6,url.lastIndexOf(pkgName));
        return path.replace("/",File.separator);
    }

    public String clzz2FileSystemPath(Class<?> clzz){
        String path = clzz.getResource("").toString();
        if(path.startsWith("jar:")){ //use maven protocol
            return classResourcePathToFileSystemWorkFolder(clzz);
        }
            return findFilenameByClassWithFileProtocol(clzz);
    }

    public boolean isClassInJar(Class<?> clzz){
        String path = clzz.getResource("").toString();
        return path.startsWith("jar:");
    }

    /**
     * 找到class所在jar的jar文件路径（全路径，包括jar名）
     * ref参数返回jar的名称，不带路径
     * @param clzz
     * @param jarSimpleName
     * @return
     */
    public String findJarFilenameByClassWithJarProtocol(Class<?> clzz, RefArg<String> jarSimpleName){
        String url = clzz.getResource("").toString();
        String path = null;
        if(url.startsWith("jar:")) {
            int jarSepIdx = url.lastIndexOf("!");
            path = url.substring(10,jarSepIdx);//jar proto must with file proto
            jarSimpleName.setValue(path.substring(path.lastIndexOf("/") + 1));
        }
        return path.replace("/",File.separator);
    }
    /**
     * file协议的路径转换成文件系统的路径
     * @return
     */
    public String findFilenameByClassWithFileProtocol(Class<?> clzz){
        String simpleClassname = clzz.getSimpleName();
        URL url = clzz.getResource("");
        String filename = url.toString() + simpleClassname + ".class";
        if(filename.startsWith("file:")) {
            filename = filename.substring(filename.indexOf(":") + 2);
            filename = filename.replace("/",File.separator);
        }
        return filename;
    }

    /**
     * 是否是普通类
     * @param type
     * @return
     */
    public boolean isNormalClass(Class<?> type){
        int mod = type.getModifiers();
        return (!Modifier.isAbstract(mod)) && (!Modifier.isInterface(mod));
    }

    /**
     * 通过classload的domain来获取jar
     * @param domain
     * @return
     */
    public String findJarFilenameByDomainWithJarProtocol(ProtectionDomain domain){
//        file:/C:/Users/xuhaifeng/.m2/repository/org/slf4j/slf4j-api/1.7.21/slf4j-api-1.7.21.jar
        String filename = domain.getCodeSource().getLocation().getFile();
        if(filename.startsWith("/")) {
            filename =  filename.substring(1);
        }
        return filename.replace("/",File.separator);
    }

    /**
     * get system classloer ProtectionDomain
     * @param loader
     * @return
     */
    public Set<String> findJarFullFilenameByPrivatedDoamins(ClassLoader loader) throws NoSuchFieldException, IllegalAccessException {
        Set<String> domains = new HashSet<>();
        Class<?> clzz = ClassLoader.class;
        Field f = clzz.getDeclaredField("domains");
        if (null == f) {
            return null;
        }
        f.setAccessible(true);
        Object obj = f.get(loader);
        Set<ProtectionDomain> loadDomains = (Set<ProtectionDomain>) obj;
        for(ProtectionDomain pd :loadDomains){
            String file = findJarFilenameByDomainWithJarProtocol(pd);
            if(file.endsWith(".jar")
                    && !file.contains("Albianj.Launcher")
//                    && !file.contains("IntelliJIdea")
                    && !file.contains("eclipse")) {
                domains.add(file);
            }
        }
        return domains;
    }

    /**
     * 得到jar的简单名称
     * @param filename
     * @return
     */
    public String getJarSimpleFilename(String filename){
        return filename.substring(filename.lastIndexOf(File.separator) + 1);
    }

//    public String clzzParentJar(Class<?> clzz) throws NoSuchFieldException, IllegalAccessException {
//        Package pkg = clzz.getPackage();
//        Set<String> domains = findJarByPrivatedDoamins(clzz.getClassLoader());
//        String pkgName = pkg.getName();
//        String implTitle = pkg.getImplementationTitle();
//        String implVersion = pkg.getImplementationVersion();
//        for(String fname :  domains) {
//            String url = domain.getCodeSource().getLocation().toString();
//            if(url.contains(pkgName) && url.contains(implTitle) &&url.contains(implVersion)) {
//                return url.substring(6); // substring with begin "file:/"
//            }
//        }
//        return null;
//    }

}
