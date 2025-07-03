/**
 * Represents an entry in an R-tree node.
 * Each entry contains a bounding rectangle, a reference to either a data block or a child node,
 * and a flag indicating whether it's a leaf entry.
 **/

import java.io.Serializable;

public class Entry implements Serializable {
    private Rectangle rectangle; //Bounding rectangle (MBR - Minimum Bounding Rectangle)
    private final long blockId;  //If leafed, refers to a block in the data file; if internal, refers to a child node block
    private final boolean isLeaf; //True if this entry is in a leaf node (points to data), false if it points to another node

    /**
     * Constructor for Entry.
     * @param rectangle The bounding rectangle for this entry.
     * @param blockId The ID of the block this entry refers to.
     * @param isLeaf Indicates if the entry is in a leaf node.
     **/
    public Entry(Rectangle rectangle, long blockId, boolean isLeaf) {
        this.rectangle = rectangle;
        this.blockId = blockId;
        this.isLeaf = isLeaf;
    }

    //Setters and getters
    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public long getBlockId() {
        return blockId;
    }

    public boolean isLeaf() {
        return isLeaf;
    }
}
