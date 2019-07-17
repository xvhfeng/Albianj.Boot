package org.albianj.framework.boot.servants;


import org.albianj.framework.boot.tags.BundleSharingTag;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@BundleSharingTag
public class FileServant {

    public static FileServant Instance = null;

    static {
        if(null == Instance) {
            Instance = new FileServant();
        }
    }

    protected FileServant() {

    }

    public boolean isFileOrPathExist(String fullname){
        File f = new File(fullname);
        return f.exists();
    }

    public boolean isFileOrPathNotExist(String fullname){
        return !this.isFileOrPathExist(fullname);
    }

    public boolean createFileParentFolder(String filename){
        File f = new File(filename);
        if(f.exists()) {
            return true;
        }

        f = f.getParentFile();
        if(f.exists()){
            return true;
        }
        return f.mkdirs();
    }

    public byte[] getFileBytes(String filename) {
        File f = new  File(filename);
        if(!f.exists()) return null;
        try {
            return  Files.readAllBytes(f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
//        InputStream ins = null;
//        ByteArrayOutputStream baos = null;
//        try {
//            if(!isFileOrPathExist(filename)) {
//                return null;
//            }
//            ins = new FileInputStream(filename);
//            baos = new ByteArrayOutputStream();
//            int bufferSize = 4096;
//            byte[] buffer = new byte[bufferSize];
//            int bytesNumRead = 0;
//            while ((bytesNumRead = ins.read(buffer)) != -1) {
//                baos.write(buffer, 0, bytesNumRead);
//            }
//            return baos.toByteArray();
//        } catch (IOException e) {
//
//        }finally {
//            if(null != ins){
//                try {
//                    ins.close();
//                } catch (IOException e) {
//
//                }
//            }
//            if(null != baos){
//                try {
//                    baos.close();
//                } catch (IOException e) {
//
//                }
//            }
//        }
//        return null;
    }

    public String makeFolderWithSuffixSep(String path){
        return path.endsWith(File.separator) ? path : path + File.separator;
    }

    /**
     * 通过后缀得到文件，包括子文件夹
     * @param folder
     * @param suffix
     * @return
     */
    public List<File> findFileBySuffix(String folder,final String suffix){
        File dir = new File(folder);
        if (!dir.isDirectory()) {
            return null;
        }

        List<File> rcFiles = new ArrayList<>();
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory() || (name.endsWith(suffix));
            }
        });
        if(null != files || 0 != files.length){
            for (File f: files) {
                if(f.isDirectory()) {
                    List<File> tmpFiles = findFileBySuffix( f.getName(), suffix);
                    rcFiles.addAll(tmpFiles);
                }else {
                    rcFiles.add(f);
                }

            }
        }
        return rcFiles;
    }

    public String getFolderName(String filename){
        File f = new File(filename);
        return f.getParentFile().toString();
    }

}
