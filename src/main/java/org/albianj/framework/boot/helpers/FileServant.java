package org.albianj.framework.boot.helpers;


import org.albianj.framework.boot.tags.BundleSharingTag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
        return !this.isFileOrPathNotExist(fullname);
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

}
