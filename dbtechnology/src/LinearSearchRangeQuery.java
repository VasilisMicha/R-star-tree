import java.util.ArrayList;

/**
 * Class that is used or performing a range query over a defined bounding box
 * Retrieves records that fall within the specified spatial area, without relying on an index
 */
class LinearSearchRangeQuery extends Query{
    private ArrayList<Long> qualifyingRecordIds;
    private BoundingBox searchBoundingBox;

    /**
     * Constructor
     * @param searchBoundingBox
     */
    LinearSearchRangeQuery(BoundingBox searchBoundingBox){
        this.searchBoundingBox = searchBoundingBox;
    }

    /**
     * @return the ids of the range query
     */
    @Override
    ArrayList<Long> getResultIds() {
        qualifyingRecordIds= new ArrayList<>();
        find();
        return qualifyingRecordIds;
    }

    @Override
    ArrayList<Long> getResultIds(Node node) {
        qualifyingRecordIds= new ArrayList<>();
        find();
        return qualifyingRecordIds;
    }

    /**
     * Finds the ids of the range query
     */
    private void find(){
        int blockId= 1;
        while(blockId< DataFileHandler.getDatafileBlockCounter()){
            ArrayList<Point> pointsInBlock;
            pointsInBlock= DataFileHandler.readDataFileBlock(blockId);
            ArrayList<LeafEntry> entries= new ArrayList<>();
            if (pointsInBlock != null){
                for (Point point : pointsInBlock){
                    ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
                    for (int d=0; d<DataFileHandler.getDataDimensions(); d++){
                        boundsForEachDimension.add(new Bounds(point.getCoords(d), point.getCoords(d)));
                    }
                    entries.add(new LeafEntry(point.getId(), blockId, boundsForEachDimension));
                }
                for(Entry entry : entries){
                    if(BoundingBox.checkOverlap(entry.getBoundingBox(), searchBoundingBox)){
                        LeafEntry leafEntry= (LeafEntry) entry;
                        qualifyingRecordIds.add(leafEntry.getRecordId());
                    }
                }
            }else{
                throw new IllegalStateException("Something went wrong when reading datafile");
            }
            blockId++;
        }
    }
}