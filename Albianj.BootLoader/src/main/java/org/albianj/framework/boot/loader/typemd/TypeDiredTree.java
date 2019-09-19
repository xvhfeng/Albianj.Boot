package org.albianj.framework.boot.loader.typemd;

import org.albianj.framework.boot.loader.TypeFileMetadata;
import org.albianj.framework.boot.servants.StringServant;

import java.util.HashMap;
import java.util.Map;

public class TypeDiredTree {
    private HashMap<String,TreeNode> rootNode;
    private volatile  int size;

    public TypeDiredTree(){
        rootNode = new HashMap<>();
    }

    /**
     * 添加文件，当fileEntry为null的时候，path为目录
     * @param path
     * @param fileEntry
     */
    public synchronized void addNode(String path,TypeFileMetadata fileEntry) {
        String[] folders = path.split("\\.");
        HashMap<String,TreeNode> node = rootNode;
        for(int i = 0;i < folders.length;i++){
            TreeNode tn = node.get(folders[i]);
            if(null == tn) {
                TreeNode n = new TreeNode();
                n.setName(folders[i]);
                if(i != folders.length -1) {
                    HashMap<String,TreeNode> newNode = new HashMap<>();
                    n.setLeaf(true);
                    n.setData(newNode);
                    node.put(folders[i], n);
                    node = newNode;
                    continue;
                }
                // the last in folders
                n.setLeaf(false);
                n.setName(folders[i]);
                n.setData(fileEntry);
                node.put(folders[i], n);
                size++;
                break;
            }

            if(null != node ) {
                if(i != (folders.length - 1) && tn.isLeaf()) {
                    node = (HashMap<String,TreeNode>) tn.getData();
                }
            }
        }
    }

    public void addNodes(Map<String,TypeFileMetadata> map){
        for(Map.Entry<String,TypeFileMetadata> entry : map.entrySet()) {
            addNode(entry.getValue().getFullClassNameWithoutSuffix(),entry.getValue());
        }
    }

    public TypeFileMetadata findNode(String fullnameWithSuffix) {
//        fullnameWithSuffix = fullnameWithSuffix.substring(0,fullnameWithSuffix.lastIndexOf("."));
        String[] folders = fullnameWithSuffix.split("\\.");
        HashMap<String,TreeNode> node = rootNode;
        TypeFileMetadata md= null;
        for(int i = 0;i<folders.length;i++){
            TreeNode tn = node.get(folders[i]);
            if(null == tn) return null;

            if(i != (folders.length - 1) && tn.isLeaf()) {
                node = (HashMap<String,TreeNode>) tn.getData();
                continue;
            }
            if((i == folders.length - 1) && !tn.isLeaf()) {
                md = (TypeFileMetadata) tn.getData();
                break;
            }

        }
        return md;
    }

    /**
     * 得到子文件
     * 当isOnlyFileEntry为true的时候，只返回文件，为false的时候，叶子节点为node（即空文件夹)也返回
     * @param path
     * @return
     */
    public Map<String,TypeFileMetadata> findChildNodes(String path,boolean isOnlyFileEntry) {
        HashMap<String,TreeNode> node = rootNode;
        if(StringServant.Instance.isNotNullAndNotEmptyAndNotAllSpace(path)) {
            String[] folders = path.split("\\.");
            for (int i = 0; i < folders.length; i++) {
                TreeNode tn = node.get(folders[i]);
                if (null == tn) {
                    return null; // not file
                }
                node = (HashMap<String, TreeNode>) tn.getData();
            }
        }
        Map<String,TypeFileMetadata> fmds = new HashMap<>();
        findChildTypeFileMetadata(node,fmds);
        return fmds;
    }

    private void  findChildTypeFileMetadata(Map<String,TreeNode> node,
                                            Map<String,TypeFileMetadata> fmds){
        for(Map.Entry<String,TreeNode> entry : node.entrySet()) {
            TreeNode tn = entry.getValue();
            if(tn.isLeaf()) {
                findChildTypeFileMetadata((Map<String,TreeNode>) tn.getData(),fmds);
            }else {
                TypeFileMetadata md = (TypeFileMetadata)tn.getData();
                fmds.put(md.getFullClassNameWithoutSuffix(),md);
            }
        }
    }


    /**
     * 得到子文件
     * 当isOnlyFileEntry为true的时候，只返回文件，为false的时候，叶子节点为node（即空文件夹)也返回
     * @param path
     * @return
     */
    public Map<String,TypeFileMetadata> findChildFileEntries(String path) {
        return findChildNodes(path,true);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
