/**
 * This class represents a leaf-level entry in a spatial index structure storing
 * (1) the actual spatial point data
 * (2) physical storage location identifiers
 * @author VasilisMicha, athinaanv
 **/

import java.io.Serializable;

class LeafEntry implements Serializable {
    private Point point;
    private int blockId;
    private int recordId;

    public LeafEntry(Point point, int blockId, int recordId){
        this.point = point;
        this.blockId = blockId;
        this.recordId = recordId;
    }

    public int getSize(){
        return point.getSize() + (Integer.SIZE / 8);
    }

    public Point getPoint() {
        return point;
    }
}