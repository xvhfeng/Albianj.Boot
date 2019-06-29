package org.albianj.framework.boot.loader;

import org.albianj.framework.boot.BundleContext;
import org.albianj.framework.boot.helpers.FileServant;
import org.albianj.framework.boot.helpers.TypeServant;
import org.albianj.framework.boot.tags.BundleSharingTag;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
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
 */
@BundleSharingTag
public class BundleClassLoader extends ClassLoader {
    private String bundleName = null;
    /**
     * key - full classname
     * value - class file metadata
     */
    private Map<String, TypeFileMetadata> clzzFileMetadatasInBin = null;
    private Map<String, TypeFileMetadata> clzzFileMetadatasInCls = null;
    private Map<String, TypeFileMetadata> clzzFileMetadatasInLib = null;

    private BundleClassLoader(String bundleName) {
        this.bundleName = bundleName;
        clzzFileMetadatasInBin = new HashMap<>();
        clzzFileMetadatasInCls = new HashMap<>();
        clzzFileMetadatasInLib = new HashMap<>();
    }

    public static BundleClassLoader newInstance(String bundleName) {
        return new BundleClassLoader(bundleName);
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;
        clazz = findLoadedClass(name);
        if (clazz != null) {
            if (resolve) {
                resolveClass(clazz);
            }
            return (clazz);
        }

        //system java class loading by app loader or sys loader
        if (name.startsWith("java.")) {
            try {
                //AppClassLoader
                ClassLoader system = ClassLoader.getSystemClassLoader();
                clazz = system.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {

            }
        }
        return this.findClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // step 1 : find from bin directory
        TypeFileMetadata cfm = clzzFileMetadatasInBin.get(name);
        // step 2 : if not find from bin directory then find from classes directory
        if (cfm == null) {
            cfm = clzzFileMetadatasInCls.get(name);
        }
        // step 3 : if not find from classes directory then find from lib directory
        if (cfm == null) {
            cfm = clzzFileMetadatasInLib.get(name);
        }

        if (cfm != null) {
            byte[] bytes = cfm.getFileContentBytes();
            return defineClass(name, bytes, 0, bytes.length);
        }

        // step 4 : if not find from lib directory then find from app classloader
        // this step for developer use IDE
        ClassLoader appLoader = ClassLoader.getSystemClassLoader(); // app loader
        Class<?> clzz = appLoader.loadClass(name);
        if (clzz != null) { //reload class by custom loader
            if (clzz.isAnnotationPresent(BundleSharingTag.class)) {
                return clzz;
            }

            //interface is not transmit annotation,so only try one by one
            Class<?>[] itfs = clzz.getInterfaces();
            if (null != itfs) {
                for (Class<?> itf : itfs) {
                    // only one interface is sharing,child class must be sharing
                    if (itf.isAnnotationPresent(BundleSharingTag.class)) {
                        return clzz;
                    }
                }
            }

            String path = TypeServant.Instance.fileProtoUrl2FileSystemPath(clzz);
            byte[] data = FileServant.Instance.getFileBytes(path);
            if (null != data) {
                return defineClass(name, data, 0, data.length);
            }
        }
        Class<?> c = Class.forName(cfm.getFullClassName()); //when use app container such as jetty then use
        if (null != c) {
            return c;
        }
        return super.findClass(name);
    }

    public void scanClassesFile(String rootFinder, String currFinder, Map<String, TypeFileMetadata> map) {
        File finder = new File(rootFinder);
        if (!finder.exists()) {

        }

        File[] files = finder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".class");
            }
        });
        for (File f : files) {
            if (f.isDirectory()) {
                scanClassesFile(rootFinder, f.getAbsolutePath(), map);
            }
            if (f.isFile()) {
                try {
                    String relFileName = getRelPath(f.getAbsolutePath(), rootFinder);
                    if (map.containsKey(relFileName)) {
                        continue;
                    }
                    byte[] bytes = FileServant.Instance.getFileBytes(relFileName);
                    Class<?> cla = defineClass(f.getAbsolutePath(), bytes, 0, bytes.length);
                    TypeFileMetadata cfm = TypeFileMetadata.makeClassFileMetadata(relFileName, bytes, rootFinder, false);
                    cfm.setType(cla);
                } catch (Exception e) {

                }
            }
        }
    }

    public void scanJarFolder(String jarFinder, Map<String, TypeFileMetadata> map) {
        File finder = new File(jarFinder);
        if (!finder.exists()) {

        }

        File[] files = finder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".jar");
            }
        });
        for (File f : files) {
            if (f.isDirectory()) {
                scanJarFolder(jarFinder, map);
            }
            if (f.isFile()) {
                try {
                    scanJarFile(f.getAbsolutePath(), map);
                } catch (Exception e) {

                }
            }
        }
    }

    public void scanJarFile(String jarFile, Map<String, TypeFileMetadata> map) {
        JarInputStream jis = null;
        try {
            jis = new JarInputStream(new FileInputStream(jarFile));
            JarEntry entry = null;
            while (null != (entry = jis.getNextJarEntry())) {
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    if (map.containsKey(name)) {
                        continue;
                    }
                    byte[] bytes = TypeServant.Instance.readJarBytes(jis);
                    Class<?> cla = defineClass(name, bytes, 0, bytes.length);
                    TypeFileMetadata cfm = TypeFileMetadata.makeClassFileMetadata(name, bytes, jarFile, true);
                    cfm.setType(cla);
                    map.put(cfm.getFullClassName(), cfm);
                }
            }
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
    public void loadAllClass(String binFolder, String classesFolder, String libFolder) {
        scanJarFolder(binFolder, clzzFileMetadatasInBin);
        scanClassesFile(classesFolder, classesFolder, clzzFileMetadatasInCls);
        scanJarFolder(libFolder, clzzFileMetadatasInLib);
    }

    /**
     * 启动进程第一步，先把classes全部load了
     *
     */
    public void loadAllClass(BundleContext bctx) {
        loadAllClass(bctx.getBinFolder(),bctx.getClassesFolder(),bctx.getLibFolder());
    }

    /**
     * 根据指定的Anno来获取所有的class，不包括抽象类与接口
     * @param markAnno 被标记的Tag
     * @param unmarkAnno 未被标记的Tag
     * @return
     */
    public Map<String,TypeFileMetadata> findNormalTypeWithAnno(Class<? extends Annotation> markAnno,Class<? extends Annotation> unmarkAnno){
        Map<String,TypeFileMetadata> map = new HashMap<>();
        findNormalTypeWithParentAndAnno(null,markAnno,unmarkAnno,clzzFileMetadatasInBin,map);
        findNormalTypeWithParentAndAnno(null,markAnno,unmarkAnno,clzzFileMetadatasInCls,map);
        findNormalTypeWithParentAndAnno(null,markAnno,unmarkAnno,clzzFileMetadatasInLib,map);
        return map;
    }

    /**
     * 根据指定的parent类/接口获取所有的普通class，不包括抽象子类与子接口
     * @param parent
     * @return
     */
    public Map<String,TypeFileMetadata> findNormalTypeWithParent(Class<?> parent){
        Map<String,TypeFileMetadata> map = new HashMap<>();
        findNormalTypeWithParentAndAnno(parent,null,null,clzzFileMetadatasInBin,map);
        findNormalTypeWithParentAndAnno(parent,null,null,clzzFileMetadatasInCls,map);
        findNormalTypeWithParentAndAnno(parent,null,null,clzzFileMetadatasInLib,map);
        return map;
    }

    public void findNormalTypeWithParentAndAnno(Class<?> parent,
                                             Class<? extends Annotation> markAnno,
                                             Class<? extends Annotation> unmarkAnno,
                                             Map<String,TypeFileMetadata> from,
                                             Map<String,TypeFileMetadata> to) {
        for(Map.Entry<String,TypeFileMetadata> entry : from.entrySet()){
            TypeFileMetadata tfm = entry.getValue();
            Class<?> clzz = tfm.getType();

            /**
             * is normal class
             * and mark by markAnno
             * and not mark by unmarkAnno
             * and not in result
             */
            boolean isConform = TypeServant.Instance.isNormalClass(clzz)
                    && ((null == parent) ? true :  tfm.getType().isAssignableFrom(parent))
                    && ((null == markAnno) ? true : clzz.isAnnotationPresent(markAnno))
                    && ((null == unmarkAnno) ? true :  !clzz.isAnnotationPresent(unmarkAnno))
                    && (!to.containsKey(tfm.getFullClassNameWithoutSuffix()));
            if(isConform){
                to.put(tfm.getFullClassNameWithoutSuffix(), tfm);
            }
        }
    }
}
