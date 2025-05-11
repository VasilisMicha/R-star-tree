public class Entry {
    private final Rectangle rectangle;
    private final long blockId; // if leaf it refers to the block id in datafile, else in refers to the child node
    private final long recordId; //null if not leaf entry
    private final boolean isLeaf;

    public Entry(Rectangle rectangle, long blockId, long recordId, boolean isLeaf) {
        this.rectangle = rectangle;
        this.blockId = blockId;
        this.recordId = recordId;
        this.isLeaf = isLeaf;
    }

    public Rectangle getRectangle() { return rectangle; }
    public long getBlockId() { return blockId; }
    public boolean isLeaf() { return isLeaf; }
}
