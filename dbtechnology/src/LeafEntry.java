import java.util.ArrayList;

/**
 * Represents a leaf entry in the R-Tree
 * it stores the recordId and a reference to the data block
 * where the record is physically stored in the data file.
 */
class LeafEntry extends Entry{
    private long recordId;
    private long dataFileBlockId;

    /**
     * Constructor
     * @param recordId
     * @param dataFileBlockId
     * @param recordBounds
     */
    LeafEntry(long recordId, long dataFileBlockId, ArrayList<Bounds> recordBounds){
        super(new BoundingBox(recordBounds));
        this.recordId= recordId;
        this.dataFileBlockId= dataFileBlockId;
    }

    /**
     * Getter
     * @return id of the record
     */
    long getRecordId(){
        return recordId;
    }

    /**
     * Getter
     * @return datafile block id
     */
    long getDataFileBlockId(){
        return dataFileBlockId;
    }
}