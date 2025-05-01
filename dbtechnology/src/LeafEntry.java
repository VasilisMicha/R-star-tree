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

    public static int getSize(){
        return point.getSize() + (Integer.SIZE / 8);
    }
}