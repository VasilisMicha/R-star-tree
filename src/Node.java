import java.io.Serializable;
import java.util.List;

public class Node implements Serializable {
    private List<Entry> entries;
    private final boolean isLeaf;
    private final long blockId;
    private long parentNodeId;  // Added parent reference

    public Node(List<Entry> entries, boolean isLeaf, long blockId) {
        this(entries, isLeaf, blockId, -1); // -1 means no parent (root)
    }

    public Node(List<Entry> entries, boolean isLeaf, long blockId, long parentNodeId) {
        this.entries = entries;
        this.isLeaf = isLeaf;
        this.blockId = blockId;
        this.parentNodeId = parentNodeId;
    }

    public List<Entry> getEntries() { return entries; }
    public boolean isLeaf() { return isLeaf; }
    public long getBlockId() { return blockId; }
    public long getParentNodeId() { return parentNodeId; }
    public void setParentNodeId(long parentNodeId) { this.parentNodeId = parentNodeId; }
    public void addEntry(Entry entry) {
        entries.add(entry);
    }
}
