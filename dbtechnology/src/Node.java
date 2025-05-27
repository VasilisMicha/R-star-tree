//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Node implements Serializable {
    private List<Entry> entries;
    private boolean isLeaf;
    private int level;
    private long blockId;
    private long parentNodeId;
    private boolean isPartOfTheTree;

    public Node(List<Entry> entries, boolean isLeaf, long blockId) {
        this(entries, isLeaf, blockId, -1L);
    }
    int getLevel() {
        return level;
    }
    public Node(List<Entry> entries, boolean isLeaf, long blockId, long parentNodeId) {
        this.isPartOfTheTree = true;
        this.entries = entries;
        this.isLeaf = isLeaf;
        this.blockId = blockId;
        this.parentNodeId = parentNodeId;
    }
    public Node(int i, ArrayList<Entry> entries) {
        //something
    }
    public ArrayList<Entry> getEntries() {
        return (ArrayList<Entry>) this.entries;
    }
    public boolean isLeaf() {
        return this.isLeaf;
    }
    public long getBlockId() {
        return this.blockId;
    }
    public long getParentNodeId() {
        return this.parentNodeId;
    }
    public boolean getIsPartOfTheTree() {
        return this.isPartOfTheTree;
    }
    public void setParentNodeId(long parentNodeId) {
        this.parentNodeId = parentNodeId;
    }
    public void setIsPartOfTheTree(boolean isPartOfTheTree) {
        this.isPartOfTheTree = isPartOfTheTree;
    }
    public void addEntry(Entry entry) {
        this.entries.add(entry);
    }
}
