/**
 * Class which is used to store an id of a record
 * and the distance from a specific item
 */
class IdDistancePair {
    private long recordId;
    private double distanceFromItem;

    /**
     * Constructor
     * @param recordId
     * @param distanceFromItem
     */
    IdDistancePair(long recordId, double distanceFromItem){
        this.recordId= recordId;
        this.distanceFromItem= distanceFromItem;
    }

    /**
     * Getter
     * @return the record id
     */
    long getRecordId(){
        return recordId;
    }

    /**
     * Getter
     * @return distance from item
     */
    double getDistanceFromItem(){
        return distanceFromItem;
    }
}