/**
 * Represents a node in a tree-based spatial index.
 * Each node contains multiple entries and metadata:
 * - entries: the list of child entries or data entries.
 * - isLeaf: true if the node is a leaf node/ false if internal.
 * - blockId: unique identifier (block number) of this node in the index file.
 * - parentNodeId: block ID of the parent node (-1 if root).
 * - isPartOfTheTree: flag indicating whether the node is currently active in the tree.
 **/

import java.io.Serializable;
import java.util.List;

public class Node implements Serializable {

    private List<Entry> entries;
    private final boolean isLeaf;
    private final long blockId;
    private long parentNodeId;
    private boolean isPartOfTheTree = true;

    //Constructor without parent (root node)
    public Node(List<Entry> entries, boolean isLeaf, long blockId) {
        this(entries, isLeaf, blockId, -1); //-1 means no parent
    }

    //Constructor with explicit parent node ID
    public Node(List<Entry> entries, boolean isLeaf, long blockId, long parentNodeId) {
        this.entries = entries;
        this.isLeaf = isLeaf;
        this.blockId = blockId;
        this.parentNodeId = parentNodeId;
    }

    //Getters/setters for class fields
    public List<Entry> getEntries() { return entries; }
    public boolean isLeaf() { return isLeaf; }
    public long getBlockId() { return blockId; }
    public long getParentNodeId() { return parentNodeId; }
    public boolean getIsPartOfTheTree() { return isPartOfTheTree; }
    public void setParentNodeId(long parentNodeId) { this.parentNodeId = parentNodeId; }
    public void setIsPartOfTheTree(boolean isPartOfTheTree) { this.isPartOfTheTree = isPartOfTheTree; }

    //Adds an entry to this node's list of entries
    public void addEntry(Entry entry) {
        entries.add(entry);
    }
}
