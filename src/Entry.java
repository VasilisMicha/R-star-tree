import java.io.Serializable;

public class Entry implements Serializable {
    private Rectangle rectangle;
    private final long blockId; // if leaf it refers to the block id in datafile, else in refers to the child node
    private final boolean isLeaf;
    public Entry(Rectangle rectangle, long blockId, boolean isLeaf) {
        this.rectangle = rectangle;
        this.blockId = blockId;
        this.isLeaf = isLeaf;
    }
    public void setRectangle(Rectangle rectangle){ this.rectangle = rectangle;}
    public Rectangle getRectangle() { return rectangle; }
    public long getBlockId() { return blockId; }
    public boolean isLeaf() { return isLeaf; }
}