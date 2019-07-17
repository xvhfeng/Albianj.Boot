package org.albianj.framework.boot.loader;

import org.albianj.framework.boot.BundleContext;
import org.albianj.framework.boot.logging.LogServant;
import org.albianj.framework.boot.logging.LoggerLevel;
import org.albianj.framework.boot.servants.FileServant;
import org.albianj.framework.boot.servants.RefArg;
import org.albianj.framework.boot.servants.TypeServant;
import org.albianj.framework.boot.tags.BundleSharingTag;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class BundlestrupClassLoader extends ClassLoader {
    private String bundleName = null;
    /**
     * key - full classname
     * value - class file metadata
     */

    protected Map<String, TypeFileMetadata> totalFileMetadatas = null;
    protected Set<String> jarFileSet = null;

    protected BundlestrupClassLoader(String bundleName) {
        super(null);
        this.bundleName = bundleName;
        totalFileMetadatas = new HashMap<>();
        jarFileSet = new HashSet<>();
    }

    public static BundleClassLoader newInstance(String bundleName) {
        return new BundleClassLoader(bundleName);
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} ready to load.", name)
                .aroundBundle(this.bundleName)
                .atLevel(LoggerLevel.Debug)
                .forSessionId("loadclass")
                .byCalled(this.getClass())
                .takeBrief("Loading Class")
                .build().toLogger();
        return loadClass(name, false);
    }

    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;
        clazz = findLoadedClass(name);
        if (clazz != null) {
            if (resolve) {
                resolveClass(clazz);
            }
            LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} was found by loaded.", name)
                    .aroundBundle(this.bundleName)
                    .atLevel(LoggerLevel.Debug)
                    .forSessionId("loadclass")
                    .byCalled(this.getClass())
                    .takeBrief("Loading Class")
                    .build().toLogger();
            return (clazz);
        }

        if (name.startsWith("org.albianj.framework.boot")) {
            try {
                LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} was system class,so loading by SystemClassLoader.", name)
                        .aroundBundle(this.bundleName)
                        .atLevel(LoggerLevel.Debug)
                        .byCalled(this.getClass())
                        .forSessionId("loadclass")
                        .takeBrief("Loading Class")
                        .build().toLogger();

                //AppClassLoader
                ClassLoader system = ClassLoader.getSystemClassLoader();
                clazz = system.loadClass(name);
                if (clazz != null && resolve) {
                    resolveClass(clazz);
                }
                return (clazz);
            } catch (ClassNotFoundException e) {
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
        }


        //system java class loading by app loader or sys loader
        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.")) {
            try {
                LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} was system class,so loading by SystemClassLoader.", name)
                        .aroundBundle(this.bundleName)
                        .atLevel(LoggerLevel.Debug)
                        .byCalled(this.getClass())
                        .forSessionId("loadclass")
                        .takeBrief("Loading Class")
                        .build().toLogger();

                //AppClassLoader
                ClassLoader system = ClassLoader.getSystemClassLoader();
                clazz = system.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
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
        }

        Class<?> clzz = null;
        try {
            clzz = this.findClass(name);
        } catch (Exception e) {
            LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} by findClass in BundleClassLoder,but loading fail.", name)
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

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} was ready to find by findClass.", name)
                .aroundBundle(this.bundleName)
                .atLevel(LoggerLevel.Debug)
                .byCalled(this.getClass())
                .forSessionId("loadclass")
                .takeBrief("Loading Class")
                .build().toLogger();

        /**
         * find from jar or classes folder
         */
        TypeFileMetadata cfm = totalFileMetadatas.get(name);
        Class<?> clzz = null;
        if (cfm != null) {
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
            cfm.setType(clzz);
            return clzz;
        }

        /**
         *  if not find from lib directory then find from system classloader
         */
        ClassLoader appLoader = ClassLoader.getSystemClassLoader(); // app loader
        try {
            clzz = appLoader.loadClass(name);
        } catch (Exception e) {
            LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} was loaded fail by App SystemClassLoader.", name)
                    .aroundBundle(this.bundleName)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .withCause(e)
                    .forSessionId("loadclass")
                    .takeBrief("Loading Class")
                    .build().toLogger();
        }

        LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} was loaded success by App SystemClassLoader,then reload by BundleClassLoader.", name)
                .aroundBundle(this.bundleName)
                .atLevel(LoggerLevel.Debug)
                .byCalled(this.getClass())
                .forSessionId("loadclass")
                .takeBrief("Loading Class")
                .build().toLogger();

        if (clzz != null) { //reload class by custom loader
            if (clzz.isAnnotationPresent(BundleSharingTag.class)) {
                LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} is BundleSharing,so not need reload.", name)
                        .aroundBundle(this.bundleName)
                        .atLevel(LoggerLevel.Debug)
                        .byCalled(this.getClass())
                        .forSessionId("loadclass")
                        .takeBrief("Loading Class")
                        .build().toLogger();

                return clzz;
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
                        LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} is BundleSharing because  it's interface -> {0} is BundleSharing,so not need reload.",
                                name, itf.getName())
                                .aroundBundle(this.bundleName)
                                .atLevel(LoggerLevel.Debug)
                                .byCalled(this.getClass())
                                .forSessionId("loadclass")
                                .takeBrief("Loading Class")
                                .build().toLogger();

                        return clzz;
                    }
                }
            }

            /**
             *  privately-owned class
             *  so we relaod it by bundle classloader in bundle range
             */
            if (TypeServant.Instance.isClassInJar(clzz)) {
                /**
                 * class in jar then use jar protocol
                 * if use maven,jarfile in the .m2 repository
                 * or maybe referenced in IDE in classpath(we not try it).
                 */
                RefArg<String> jarSimpleName = new RefArg<>();
                String jarName = TypeServant.Instance.findJarFilenameByClassWithJarProtocol(clzz, jarSimpleName);

                LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} is in jarFile ->{1}.so unpacking the jar and loading it.",
                        name, jarName)
                        .aroundBundle(this.bundleName)
                        .atLevel(LoggerLevel.Debug)
                        .byCalled(this.getClass())
                        .forSessionId("loadclass")
                        .takeBrief("Loading Class")
                        .build().toLogger();
                scanJarFile(jarName, jarName, totalFileMetadatas);
                if (!totalFileMetadatas.containsKey(name)) {
                    /**
                     * interface and impl-class is not loading
                     * so we need load  from system classloader
                     */
                    try {
                        Set<String> jarFilenames = TypeServant.Instance.findJarFullFilenameByPrivatedDoamins(clzz.getClassLoader());
                        LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} is not in jarFile ->{1}.so we try it from PrivatedDoamins.",
                                name, jarName)
                                .aroundBundle(this.bundleName)
                                .atLevel(LoggerLevel.Debug)
                                .byCalled(this.getClass())
                                .forSessionId("loadclass")
                                .takeBrief("Loading Class")
                                .build().toLogger();
                        for (String jarFilename : jarFilenames) {
                            scanJarFile(FileServant.Instance.getFolderName(jarFilename), jarFilename, totalFileMetadatas);
                        }
                    } catch (Exception e) {
                        LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} loading by app classloader PrivatedDomain was fail.", name)
                                .aroundBundle(this.bundleName)
                                .atLevel(LoggerLevel.Error)
                                .byCalled(this.getClass())
                                .alwaysThrow(true)
                                .withCause(e)
                                .forSessionId("loadclass")
                                .takeBrief("Loading Class")
                                .build().toLogger();
                    }
                }
                if (!totalFileMetadatas.containsKey(name)) {
                    LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} not found by all FileMetadatas.", name)
                            .aroundBundle(this.bundleName)
                            .atLevel(LoggerLevel.Error)
                            .byCalled(this.getClass())
                            .alwaysThrow(true)
                            .forSessionId("loadclass")
                            .takeBrief("Loading Class")
                            .build().toLogger();
                }
                clzz = findClass(name);
                return clzz;
            } else {
                /**
                 * class in filesystem then use file protocol
                 */
                String path = TypeServant.Instance.findFilenameByClassWithFileProtocol(clzz);
                LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} is in systemfile ->{1}.so loading it.",
                        name, path)
                        .aroundBundle(this.bundleName)
                        .atLevel(LoggerLevel.Debug)
                        .byCalled(this.getClass())
                        .forSessionId("loadclass")
                        .takeBrief("Loading Class")
                        .build().toLogger();

                byte[] data = FileServant.Instance.getFileBytes(path);
                if (null != data) {
                    return defineClass(name, data, 0, data.length);
                }
            }
        }

        LogServant.Instance.newLogPacketBuilder().addMessage("Class -> {0} loading by BundleClassLoader is fail.try use the third classload to load it in possible.",
                name)
                .aroundBundle(this.bundleName)
                .atLevel(LoggerLevel.Debug)
                .byCalled(this.getClass())
                .forSessionId("loadclass")
                .takeBrief("Loading Class")
                .build().toLogger();

        /**
         *  try with third classloader at last time in possible
         */
        Class<?> c = Class.forName(cfm.getFullClassName()); //when use app container such as jetty then use
        if (null != c) {
            return c;
        }
        return super.findClass(name);
    }

    public void scanClassesFile(String fromFolder, String rootFinder, String currFinder, Map<String, TypeFileMetadata> map) {
        File finder = new File(rootFinder);
        if (!finder.exists()) {

        }

        File[] files = finder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".class");
            }
        });
        if (null == files || 0 == files.length) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                scanClassesFile(fromFolder, rootFinder, f.getAbsolutePath(), map);
            }
            if (f.isFile()) {
                try {
                    String relFileName = getRelPath(f.getAbsolutePath(), rootFinder);
                    if (map.containsKey(relFileName)) {
                        continue;
                    }
                    byte[] bytes = FileServant.Instance.getFileBytes(relFileName);
                    /**
                     * scan file not like jarfile so it can define class in the method
                     * scan a folder for classes just only happend at begin and use classes folde,
                     * it never use for scan all classes not loaded by app classloader
                     *
                     */
                    Class<?> cla = defineClass(f.getAbsolutePath(), bytes, 0, bytes.length);
                    TypeFileMetadata cfm = TypeFileMetadata.makeClassFileMetadata(relFileName, bytes, rootFinder, false, fromFolder);
                    cfm.setType(cla);
                } catch (Exception e) {

                }
            }
        }
    }

    public void scanJarFolder(String fromFolder, String jarFinder, Map<String, TypeFileMetadata> map) {
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
        if (null == files || 0 == files.length) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                scanJarFolder(fromFolder, jarFinder, map);
            }
            if (f.isFile()) {
                try {
                    scanJarFile(fromFolder, f.getAbsolutePath(), map);
                } catch (Exception e) {

                }
            }
        }
    }

    public void scanJarFile(String fromFolder, String jarFile, Map<String, TypeFileMetadata> map) {
        if (jarFileSet.contains(jarFile)) {
            return;
        }
        JarInputStream jis = null;
        try {
            jis = new JarInputStream(new FileInputStream(jarFile));
            sacnSingleJarBytes(fromFolder, jarFile, map, jis);
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

    protected void sacnSingleJarBytes(String fromFolder, String jarFile, Map<String, TypeFileMetadata> map, JarInputStream jis) throws IOException {
        JarEntry entry = null;
        while (null != (entry = jis.getNextJarEntry())) {
            String name = entry.getName();
            if (name.endsWith(".class")) {
                if (map.containsKey(name)) {
                    continue;
                }
                byte[] bytes = TypeServant.Instance.readJarBytes(jis);
                name = name.replace("/", ".");
                TypeFileMetadata cfm = TypeFileMetadata.makeClassFileMetadata(name, bytes, jarFile, true, fromFolder);
                map.put(cfm.mkKey(), cfm);
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
    private void loadAllClass(String binFolder, String classesFolder, String libFolder) {
        /**
         * because load priority,so  low priority loading before high priority
         */
        scanJarFolder("lib", libFolder, totalFileMetadatas);
        scanClassesFile("classes", classesFolder, classesFolder, totalFileMetadatas);
        scanJarFolder("bin", binFolder, totalFileMetadatas);

        for (Map.Entry<String, TypeFileMetadata> entry : totalFileMetadatas.entrySet()) {
            LogServant.Instance.newLogPacketBuilder().addMessage("Scan All Class -> {0}.",
                    entry.getValue().getFullClassNameWithoutSuffix())
                    .aroundBundle(this.bundleName)
                    .atLevel(LoggerLevel.Debug)
                    .byCalled(this.getClass())
                    .forSessionId("loadclass")
                    .takeBrief("Loading Class")
                    .build().toLogger();
        }

    }

    /**
     * 启动进程第一步，先把classes全部load了
     */
    public void loadAllClass(BundleContext bctx) {
        loadAllClass(bctx.getBinFolder(), bctx.getClassesFolder(), bctx.getLibFolder());
    }

    /**
     * 按照主从合并类元素，
     *
     * @param map
     * @param isReplaceIfExist，当为true，参数map为master，其将替换原total中的类（如果有的话)
     */
    protected void mergerTypeFileMetadata(Map<String, TypeFileMetadata> map, boolean isReplaceIfExist) {
        if (isReplaceIfExist) {
            this.totalFileMetadatas.putAll(map);
        } else {
            map.putAll(this.totalFileMetadatas);
            this.totalFileMetadatas = map;
        }
    }

    /**
     * 根据指定的Anno来获取所有的class，不包括抽象类与接口
     *
     * @param markAnno   被标记的Tag
     * @param unmarkAnno 未被标记的Tag
     * @return
     */
    public Map<String, TypeFileMetadata> findNormalTypeWithAnno(Class<? extends Annotation> markAnno, Class<? extends Annotation> unmarkAnno) {
        Map<String, TypeFileMetadata> map = new HashMap<>();
        findNormalTypeWithParentAndAnno(null, markAnno, unmarkAnno, totalFileMetadatas, map);
        return map;
    }

    /**
     * 根据指定的parent类/接口获取所有的普通class，不包括抽象子类与子接口
     *
     * @param parent
     * @return
     */
    public Map<String, TypeFileMetadata> findNormalTypeWithParent(Class<?> parent) {
        Map<String, TypeFileMetadata> map = new HashMap<>();
        findNormalTypeWithParentAndAnno(parent, null, null, totalFileMetadatas, map);
        return map;
    }

    public void findNormalTypeWithParentAndAnno(Class<?> parent,
                                                Class<? extends Annotation> markAnno,
                                                Class<? extends Annotation> unmarkAnno,
                                                Map<String, TypeFileMetadata> from,
                                                Map<String, TypeFileMetadata> to) {
        for (Map.Entry<String, TypeFileMetadata> entry : from.entrySet()) {
            TypeFileMetadata tfm = entry.getValue();
            Class<?> clzz = tfm.getType();

            /**
             * is normal class
             * and mark by markAnno
             * and not mark by unmarkAnno
             * and not in result
             */
            boolean isConform = TypeServant.Instance.isNormalClass(clzz)
                    && ((null == parent) ? true : tfm.getType().isAssignableFrom(parent))
                    && ((null == markAnno) ? true : clzz.isAnnotationPresent(markAnno))
                    && ((null == unmarkAnno) ? true : !clzz.isAnnotationPresent(unmarkAnno))
                    && (!to.containsKey(tfm.mkKey()));
            if (isConform) {
                to.put(tfm.mkKey(), tfm);
            }
        }
    }
}
