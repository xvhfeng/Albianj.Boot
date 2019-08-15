package org.albianj.framework.boot.loader.typemd;

public class TreeNode {
    private String name;
    private boolean isLeaf;
    /*
        if isLeaf is true means current node is leaf,data is TreeSet<TreeNode>
        else then current node is file,data is TypeFileMetadata
     */
    private Object data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
