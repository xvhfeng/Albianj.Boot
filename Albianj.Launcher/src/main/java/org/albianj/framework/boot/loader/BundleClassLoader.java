package org.albianj.framework.boot.loader;

import org.albianj.framework.boot.BundleContext;
import org.albianj.framework.boot.loader.typemd.TypeDiredTree;
import org.albianj.framework.boot.logging.LogServant;
import org.albianj.framework.boot.logging.LoggerLevel;
import org.albianj.framework.boot.servants.FileServant;
import org.albianj.framework.boot.servants.StringServant;
import org.albianj.framework.boot.servants.TypeServant;
import org.albianj.framework.boot.tags.BundleSharingTag;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * 每个bundle一个classloader，这样可以保证不同的bundle之间使用不同版本的class或者类库
 * classloader使用“已有不加载”原则，或者也叫“第一次见加载”原则进行加载。
 * classloader的加载顺序为：
 * 1. bin文件夹  一般将带有main函数的jar放入到bin文件夹
 * 2. classes文件夹  classes中的所有class文件
 * 3. lib文件夹  lib文件夹中的jar文件一般为公共库的jar
 * <p>
 * 另外一种方式：
 * 直接使用“私有库”方式，或者称之为：jar模式。
 * 即直接将所有的jar全部放入到bin文件夹中
 * 这种情况下，classes文件夹的class将会第二步才能加载，
 * 故依据已有同类不加载原则，classes中的class不会具有替换bin中的功能。
 * <p>
 * 对于不同版本的jar，不做版本区分加载。并且参照“第一次见”原则加载
 * <p>
 * 为了解决在IDE中的使用问题，必须打破classloader加载原则，重新加载class
 *
 * v2:
 * 最后决定还是放弃兼容ide等模式
 * 主要的问题在于：当使用maven等包管理器的时候，依赖的依赖（并且不是complile的依赖）
 * 基本上大部分都无法找到相应的jar来加载，试过使用getResource，试过PrivatedDomain通过反射来做，
 * 都只能解决一部分的问题，不能解决所有的问题，并且导致了classloader功能复杂无比，代码膨胀太大
 * so，最后决定放弃之。
 * 取而代之的是，在使用包管理器的情况下（以maven为例），需要加入一个build的配置项：
 * 当我们编译项目的时候，把项目的所有依赖包全部加载到一个文件夹下，然后通过classlaoder
 * 在启动的时候扫描这个文件夹来预加载所有的class。
 */
@BundleSharingTag
public class BundleClassLoader extends ClassLoader {
    private String bundleName = null;
    private ClassLoader parent = null;
    /**
     * key - full classname
     * value - class file metadata
     */

//    protected Map<String,TypeFileMetadata> totalFileMetadatas = null;
    protected TypeDiredTree typeDiredTree = null;
    protected Set<String> jarFileSet = null;

    protected BundleClassLoader(String bundleName) {
        super(BundleClassLoader.class.getClassLoader());
        this.parent = BundleClassLoader.class.getClassLoader();//Thread.currentThread().getContextClassLoader();
        this.bundleName = bundleName;
//        totalFileMetadatas = new HashMap<>();
        jarFileSet = new HashSet<>();
        typeDiredTree = new TypeDiredTree();
    }

    public static BundleClassLoader newInstance(String bundleName) {
        return new BundleClassLoader(bundleName);
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} ready to load.",name)
                .aroundBundle(this.bundleName)
                .atLevel(LoggerLevel.Debug)
                .forSessionId("loadclass")
                .byCalled(this.getClass())
                .takeBrief("Loading Class")
                .build().toLogger();
        if(name.equals("com.mysql.jdbc.LocalizedErrorMessages_zh")) {
            return null;
        }
        return loadClass(name, false);
    }

    @Override
    protected synchronized Class<?> findClass(String name) throws ClassNotFoundException {
        Class clzz = null;
        clzz = findLoadedClass(name);
        if (clzz != null) {
            LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} was found by loaded.", name)
                    .aroundBundle(this.bundleName)
                    .atLevel(LoggerLevel.Debug)
                    .forSessionId("loadclass")
                    .byCalled(this.getClass())
                    .takeBrief("Loading Class")
                    .build().toLogger();
            return (clzz);
        }

        /**
         * loader must loading by app or extloader
         */
        if (!name.startsWith("org.albianj.framework.boot")) {
//            if (totalFileMetadatas.containsKey(name)) {
            if(null != typeDiredTree.findNode(name)) {
                clzz = loadClassFromTypeMetadata(name);
            }

            if (null != clzz) {
                return clzz;
            }
        }

        //system java class loading by app loader or sys loader
        try {
            LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} was system class,so loading by SystemClassLoader.", name)
                    .aroundBundle(this.bundleName)
                    .atLevel(LoggerLevel.Debug)
                    .byCalled(this.getClass())
                    .forSessionId("loadclass")
                    .takeBrief("Loading Class")
                    .build().toLogger();

            //AppClassLoader
            clzz = this.getParent().loadClass(name);
            if (clzz != null) {
                return (clzz);
            }
        } catch (ClassNotFoundException e) {
            LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} was system class,so loading by SystemClassLoader,but loading fail.", name)
                    .aroundBundle(this.bundleName)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .withCause(e)
                    .forSessionId("loadclass")
                    .takeBrief("Loading Class")
                    .build().toLogger();
        }

        try {
            String fileName = name.substring(name.lastIndexOf(".") + 1) + ".class";
            InputStream is = getClass().getResourceAsStream(fileName);
            if (is == null) {
                throw new ClassNotFoundException(name);
            }
            byte[] b = new byte[is.available()];
            is.read(b);
            clzz =  defineClass(name, b, 0, b.length);
            if(null == clzz) {
                throw new ClassNotFoundException(name);
            }
        } catch (Exception e) {
            LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} was system class,so loading by SystemClassLoader,but loading fail.", name)
                    .aroundBundle(this.bundleName)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .withCause(e)
                    .forSessionId("loadclass")
                    .takeBrief("Loading Class")
                    .build().toLogger();
        }
        return clzz;
    }


//    @Override
//    public URL getResource(String name) {
//        URL url = super.getResource(name);
//        LogServant.Instance.newLogPacketBuilder().addMessage("find resource -> {0} bu url -> {1}",
//                name,url)
//                .aroundBundle(this.bundleName)
//                .atLevel(LoggerLevel.Debug)
//                .forSessionId("LoadResource")
//                .byCalled(this.getClass())
//                .takeBrief("Loading Resource By Scan Jar")
//                .build().toLogger();
//        return url;
//    }

    @Override
    protected URL findResource(String name) {
        LogServant.Instance.newLogPacketBuilder().addMessage("find resource -> {0}",
                name)
                .aroundBundle(this.bundleName)
                .atLevel(LoggerLevel.Debug)
                .forSessionId("LoadResource")
                .byCalled(this.getClass())
                .takeBrief("Loading Resource By Scan Jar")
                .build().toLogger();

        URL url = super.findResource(name);
        if(null != url) {
            return url;
        }

        String className  = name.substring(0,name.lastIndexOf("."));
        className = className.replace("/",".");

        TypeFileMetadata cfm = typeDiredTree.findNode(className);
        if(null == cfm) {
            LogServant.Instance.newLogPacketBuilder().addMessage(
                    "Not Found resource -> {0} with className -> {1}.",
                    name,className)
                    .aroundBundle(this.bundleName)
                    .atLevel(LoggerLevel.Debug)
                    .forSessionId("LoadResource")
                    .byCalled(this.getClass())
                    .takeBrief("Found Resource")
                    .build().toLogger();
            return null;
        }
        url = cfm.makeFileURL();
        if(null != url){
            LogServant.Instance.newLogPacketBuilder().addMessage(
                    "find resource -> {0} with className -> {2} URL ->{1}",
                    name,url.toString(),className)
                    .aroundBundle(this.bundleName)
                    .atLevel(LoggerLevel.Debug)
                    .forSessionId("LoadResource")
                    .byCalled(this.getClass())
                    .takeBrief("Found Resource")
                    .build().toLogger();
        } else {
            LogServant.Instance.newLogPacketBuilder().addMessage("find resource -> {0} with className -> {1} not url is NULL",
                    name,className)
                    .aroundBundle(this.bundleName)
                    .atLevel(LoggerLevel.Debug)
                    .forSessionId("LoadResource")
                    .byCalled(this.getClass())
                    .takeBrief("Found Resource")
                    .build().toLogger();
        }
        return url;
    }


    protected Class<?> loadClassFromTypeMetadata(String name) {
        TypeFileMetadata cfm = typeDiredTree.findNode(name); //totalFileMetadatas.get(name);
        Class<?> clzz = null;
        if (cfm == null) {

        }

        if (null != cfm.getType()) {
            return cfm.getType();
        }

        byte[] bytes = cfm.getFileContentBytes();
        try {
            LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} was found by TypeFileMetadata and ready to loading it.", name)
                    .aroundBundle(this.bundleName)
                    .atLevel(LoggerLevel.Debug)
                    .byCalled(this.getClass())
                    .forSessionId("loadclass")
                    .takeBrief("Loading Class")
                    .build().toLogger();

            clzz = defineClass(name, bytes, 0, bytes.length);
        } catch (Exception e) {
            LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} defined by BundleClassLoder from TypeFileMetadate was fail .", name)
                    .aroundBundle(this.bundleName)
                    .atLevel(LoggerLevel.Error)
                    .alwaysThrow(true)
                    .byCalled(this.getClass())
                    .withCause(e)
                    .forSessionId("loadclass")
                    .takeBrief("Loading Class")
                    .build().toLogger();
        }

        boolean isBundleSharing = false;
        String itfName = null;
        if (clzz != null) { //reload class by custom loader
            if (clzz.isAnnotationPresent(BundleSharingTag.class)) {
                isBundleSharing = true;
            }

            /**
             *  interface is not transmit annotation,
             *  so we check everyone and try one by one
             */
            Class<?>[] itfs = clzz.getInterfaces();
            if (null != itfs) {
                for (Class<?> itf : itfs) {
                    // only one interface is sharing,child class must be sharing
                    if (itf.isAnnotationPresent(BundleSharingTag.class)) {
                        isBundleSharing = true;
                        itfName = itf.getName();
                    }
                }
            }

            if(isBundleSharing) {
                LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} is BundleSharing because it has BundleSharingTag or interface -> {0} is BundleSharing.",
                        name, StringServant.Instance.isNullOrEmpty(itfName) ? "[NULL]" : itfName)
                        .aroundBundle(this.bundleName)
                        .atLevel(LoggerLevel.Debug)
                        .byCalled(this.getClass())
                        .forSessionId("loadclass")
                        .takeBrief("Loading Class")
                        .build().toLogger();
                clzz = super.defineClass(name, bytes, 0, bytes.length);
            }
            cfm.setType(clzz);
        }
        return clzz;
    }

    public void scanClassesFile(String fromFolder,String rootFinder, String currFinder,  TypeDiredTree typeDiredTree) {
        File finder = new File(currFinder);
        if (!finder.exists()) {
            return;
        }

        File[] files = finder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".class");
            }
        });
        if(null == files || 0 == files.length) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                scanClassesFile(fromFolder,rootFinder, f.getAbsolutePath(), typeDiredTree);
            }
            if (f.isFile()) {
                try {
                    String fullFilename = f.getAbsolutePath();
                    String relFileName = fullFilename.substring(rootFinder.length()+ 1);
                    String classname = relFileName.substring(0,relFileName.lastIndexOf("."))
                            .replace("/",".").replace(File.separator,".");
                    if (null != typeDiredTree.findNode(classname)) {
                        continue;
                    }
                    byte[] bytes = FileServant.Instance.getFileBytes(fullFilename);


                    /**
                     * scan file not like jarfile so it can define class in the method
                     * scan a folder for classes just only happend at begin and use classes folde,
                     * it never use for scan all classes not loaded by app classloader
                     *
                     */

                    if (relFileName.endsWith(".class")) {
                        TypeFileMetadata cfm = TypeFileMetadata.makeClassFileMetadata(TypeFileOpt.Class,".class",relFileName, bytes, rootFinder, false,fromFolder);
                        typeDiredTree.addNode(cfm.getFullClassNameWithoutSuffix(),cfm);
                    }
                    if(relFileName.endsWith(".properties")) {
                        TypeFileMetadata cfm = TypeFileMetadata.makeClassFileMetadata(TypeFileOpt.Resource,".properties",relFileName, bytes, rootFinder, false,fromFolder);
                        typeDiredTree.addNode(cfm.getFullClassNameWithoutSuffix(),cfm);
                    }
                    if(relFileName.endsWith(".dtd")) {
                        TypeFileMetadata cfm = TypeFileMetadata.makeClassFileMetadata(TypeFileOpt.Resource,".dtd",relFileName, bytes, rootFinder, false,fromFolder);
                        typeDiredTree.addNode(cfm.getFullClassNameWithoutSuffix(),cfm);
                    }

//                    TypeFileMetadata cfm = TypeFileMetadata.makeClassFileMetadata(relFileName, bytes, rootFinder, false,fromFolder);
//                    cfm.setType(cla);
//                    typeDiredTree.addNode(cfm.getFullClassNameWithoutSuffix(),cfm);
                } catch (Exception e) {

                }
            }
        }
    }

    public void scanJarFolder(String fromFolder,String jarFinder,  TypeDiredTree typeDiredTree) {
        LogServant.Instance.newLogPacketBuilder().addMessage("Scan jar folder -> {0}",jarFinder)
                .aroundBundle(this.bundleName)
                .atLevel(LoggerLevel.Debug)
                .forSessionId("loadclass")
                .byCalled(this.getClass())
                .takeBrief("Loading Class By Scan Jar")
                .build().toLogger();

        File finder = new File(jarFinder);
        if (!finder.exists()) {
            return;
        }

        File[] files = finder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".jar");
            }
        });
        if(null == files || 0 == files.length){
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                scanJarFolder(fromFolder,f.getAbsolutePath(), typeDiredTree);
            }
            if (f.isFile()) {
                try {
                    scanJarFile(fromFolder,f.getAbsolutePath(), typeDiredTree);
                } catch (Exception e) {

                }
            }
        }
    }

    public void scanJarFile(String fromFolder,String jarFile,  TypeDiredTree typeDiredTree) {
        if (jarFileSet.contains(jarFile)) {
            return;
        }
        JarInputStream jis = null;
        try {
            jis = new JarInputStream(new FileInputStream(jarFile));
            sacnSingleJarBytes(fromFolder, jarFile, typeDiredTree, jis);
            jarFileSet.add(jarFile);
        } catch (Exception e) {

        } finally {
            if (null != jis) {
                try {
                    jis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    protected void sacnSingleJarBytes(String fromFolder, String jarFile,  TypeDiredTree typeDiredTree, JarInputStream jis) throws IOException {
        JarEntry entry = null;
        while (null != (entry = jis.getNextJarEntry())) {
            String name = entry.getName();
            String className = name.replace("/",".");
            if (null != typeDiredTree.findNode(className)) {
                continue;
            }

            if (name.endsWith(".class")) {
                byte[] bytes = TypeServant.Instance.readJarBytes(jis);
                TypeFileMetadata cfm = TypeFileMetadata.makeClassFileMetadata(TypeFileOpt.Class,".class",name, bytes, jarFile, true,fromFolder);
                typeDiredTree.addNode(cfm.getFullClassNameWithoutSuffix(),cfm);
            }
            if(name.endsWith(".properties")) {
                byte[] bytes = TypeServant.Instance.readJarBytes(jis);
                TypeFileMetadata cfm = TypeFileMetadata.makeClassFileMetadata(TypeFileOpt.Resource,".properties",name, bytes, jarFile, true,fromFolder);
                typeDiredTree.addNode(cfm.getFullClassNameWithoutSuffix(),cfm);
            }
            if(name.endsWith(".dtd")) {
                byte[] bytes = TypeServant.Instance.readJarBytes(jis);
                TypeFileMetadata cfm = TypeFileMetadata.makeClassFileMetadata(TypeFileOpt.Resource,".dtd",name, bytes, jarFile, true,fromFolder);
                typeDiredTree.addNode(cfm.getFullClassNameWithoutSuffix(),cfm);
            }
        }
    }

    private String getRelPath(String absPath, String rootPath) {
        if (absPath.startsWith(rootPath)) {
            String rel = absPath.substring(rootPath.length());
            if (rel.startsWith(File.pathSeparator)) {
                return rel.substring(File.pathSeparator.length());
            }
            return rel;
        }
        return absPath;
    }

    /**
     * 启动进程第一步，先把classes全部load了
     *
     * @param binFolder
     * @param classesFolder
     * @param libFolder
     */
    private void scanAllClass(String binFolder, String classesFolder, String libFolder,boolean isPrintScanClasses) {
        /**
         * because load priority,so  low priority loading before high priority
         */
        scanJarFolder("lib",libFolder, typeDiredTree);
        scanClassesFile("classes",classesFolder, classesFolder, typeDiredTree);
        scanJarFolder("bin",binFolder, typeDiredTree);

        LogServant.Instance.newLogPacketBuilder().addMessage("Scan All Class Count-> {0}.",
                typeDiredTree.getSize())
                .aroundBundle(this.bundleName)
                .atLevel(LoggerLevel.Debug)
                .byCalled(this.getClass())
                .forSessionId("loadclass")
                .takeBrief("Scaning Class")
                .build().toLogger();

        if(isPrintScanClasses) {
            Map<String,TypeFileMetadata> map = this.typeDiredTree.findChildNodes("",true);
            for(Map.Entry<String, TypeFileMetadata> entry :map.entrySet()) {
                LogServant.Instance.newLogPacketBuilder().addMessage("Scan All Class -> {0}. It's key -> {1}.",
                        entry.getValue().getFullClassNameWithoutSuffix(),entry.getKey())
                        .aroundBundle(this.bundleName)
                        .atLevel(LoggerLevel.Debug)
                        .byCalled(this.getClass())
                        .forSessionId("loadclass")
                        .takeBrief("Scaning Class")
                        .build().toLogger();
            }
        }
    }

    /**
     * 启动进程第一步，先把classes全部load了
     *
     */
    public void scanAllClass(BundleContext bctx) {
        scanAllClass(bctx.getBinFolder(),
                bctx.getClassesFolder(),
                bctx.getLibFolder(),
                bctx.isPrintScanClasses());
    }

    /**
     * 得到子文件
     * 当isOnlyFileEntry为true的时候，只返回文件，为false的时候，叶子节点为node（即空文件夹)也返回
     * @param path
     * @return
     */
    public Map<String,TypeFileMetadata> findChildFileEntries(String path) {
        return this.typeDiredTree.findChildNodes(path,true);
    }

    /**
     * 按照主从合并类元素，
     * @param map
     * @param isReplaceIfExist，当为true，参数map为master，其将替换原total中的类（如果有的话)
     */
    protected void mergerTypeFileMetadata(Map<String,TypeFileMetadata> map,boolean isReplaceIfExist){
//        if(isReplaceIfExist) {
           this.typeDiredTree.addNodes(map);
//        } else {
//            map.putAll(this.totalFileMetadatas);
//            this.totalFileMetadatas = map;
//        }
    }
//
//    /**
//     * 根据指定的Anno来获取所有的class，不包括抽象类与接口
//     * @param markAnno 被标记的Tag
//     * @param unmarkAnno 未被标记的Tag
//     * @return
//     */
//    public Map<String,TypeFileMetadata> findNormalTypeWithAnno(Class<? extends Annotation> markAnno,Class<? extends Annotation> unmarkAnno){
//        Map<String,TypeFileMetadata> map = new HashMap<>();
//        findNormalTypeWithParentAndAnno(null,markAnno,unmarkAnno,totalFileMetadatas,map);
//        return map;
//    }
//
//    /**
//     * 根据指定的parent类/接口获取所有的普通class，不包括抽象子类与子接口
//     * @param parent
//     * @return
//     */
//    public Map<String,TypeFileMetadata> findNormalTypeWithParent(Class<?> parent){
//        Map<String,TypeFileMetadata> map = new HashMap<>();
//        findNormalTypeWithParentAndAnno(parent,null,null,totalFileMetadatas,map);
//        return map;
//    }
//
//    public void findNormalTypeWithParentAndAnno(Class<?> parent,
//                                             Class<? extends Annotation> markAnno,
//                                             Class<? extends Annotation> unmarkAnno,
//                                             Map<String,TypeFileMetadata> from,
//                                             Map<String,TypeFileMetadata> to) {
//        for(Map.Entry<String,TypeFileMetadata> entry : from.entrySet()){
//            TypeFileMetadata tfm = entry.getValue();
//            Class<?> clzz = tfm.getType();
//
//            /**
//             * is normal class
//             * and mark by markAnno
//             * and not mark by unmarkAnno
//             * and not in result
//             */
//            boolean isConform = TypeServant.Instance.isNormalClass(clzz)
//                    && ((null == parent) ? true :  tfm.getType().isAssignableFrom(parent))
//                    && ((null == markAnno) ? true : clzz.isAnnotationPresent(markAnno))
//                    && ((null == unmarkAnno) ? true :  !clzz.isAnnotationPresent(unmarkAnno))
//                    && (!to.containsKey(tfm.mkKey()));
//            if(isConform){
//                to.put(tfm.mkKey(), tfm);
//            }
//        }
//    }
//



}
