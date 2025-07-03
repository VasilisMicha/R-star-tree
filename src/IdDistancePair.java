/**
 * Class used to store a record ID and its distance from a specific item.
 * Typically used in distance-based queries (k-NN search).
 **/

class IdDistancePair {
    private long recordId;           //ID of the record
    private double distanceFromItem; //Distance from a target item or query point

    /**
     * Constructor to initialize the record ID and distance.
     * @param recordId The ID of the record.
     * @param distanceFromItem The distance between the record and the item.
     **/
    IdDistancePair(long recordId, double distanceFromItem) {
        this.recordId = recordId;
        this.distanceFromItem = distanceFromItem;
    }
    //Getter for the record ID.
    long getRecordId() {
        return recordId;
    }
    //Getter for the distance from the item.
    double getDistanceFromItem() {
        return distanceFromItem;
    }
}
