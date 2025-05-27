import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Class that is used to perform a k-nearest neighbours search for a given query point
 * It does not use any indexing structure
 */
class LinearSearchNearestNeighbours extends Query{
    private ArrayList<Double> searchPoint;
    private int k;
    private PriorityQueue<IdDistancePair> nearestNeighbours;
    LinearSearchNearestNeighbours(ArrayList<Double> searchPoint, int k){
        if (k < 0){
            throw new IllegalArgumentException("Parameter 'k' for the nearest neighbours must be a positive number.");
        }
        this.searchPoint= searchPoint;
        this.k= k;
        this.nearestNeighbours= new PriorityQueue<>(k, new Comparator<IdDistancePair>(){
            @Override
            public int compare(IdDistancePair recordDistancePairA, IdDistancePair recordDistancePairB){
                return Double.compare(recordDistancePairB.getDistanceFromItem(),recordDistancePairA.getDistanceFromItem());
            }
        });
    }

    /**
     * @return the ids of the nearest neighbours
     */
    @Override
    ArrayList<Long> getResultIds() {
        ArrayList<Long> nearestNeighborIds= new ArrayList<>();
        findNeighbours();
        while (nearestNeighbours.size() != 0){
            IdDistancePair recordDistancePair= nearestNeighbours.poll();
            nearestNeighborIds.add(recordDistancePair.getRecordId());
        }
        Collections.reverse(nearestNeighborIds);
        return nearestNeighborIds;
    }

    @Override
    ArrayList<Long> getResultIds(Node node) {
        ArrayList<Long> nearestNeighborIds= new ArrayList<>();
        findNeighbours();
        while (nearestNeighbours.size() != 0){
            IdDistancePair recordDistancePair= nearestNeighbours.poll();
            nearestNeighborIds.add(recordDistancePair.getRecordId());
        }
        Collections.reverse(nearestNeighborIds);
        return nearestNeighborIds;
    }

    /**
     * Finds the nearest neighbours
     */
    private void findNeighbours(){
        int blockId= 1;
        while(blockId < DataFileHandler.getDatafileBlockCounter())
        {
            ArrayList<Point> pointsInBlock;
            pointsInBlock = DataFileHandler.readDataFileBlock(blockId);
            ArrayList<LeafEntry> entries = new ArrayList<>();
            if (pointsInBlock != null){
                for (Point point : pointsInBlock){
                    ArrayList<Bounds> boundsForEachDimension= new ArrayList<>();
                    for (int d=0; d<DataFileHandler.getDataDimensions(); d++){
                        boundsForEachDimension.add(new Bounds(point.getCoords(d), point.getCoords(d)));
                    }
                    entries.add(new LeafEntry(point.getId(), blockId, boundsForEachDimension));
                }
                int i=0;
                while(i<entries.size()){
                    double distanceFromPoint=entries.get(i).getBoundingBox().findMinDistanceFromPoint(searchPoint);
                    if(nearestNeighbours.size() == k){
                        if(distanceFromPoint< nearestNeighbours.peek().getDistanceFromItem()){
                            nearestNeighbours.poll();
                            nearestNeighbours.add(new IdDistancePair(entries.get(i).getRecordId(), distanceFromPoint));
                        }
                    }else{
                        nearestNeighbours.add(new IdDistancePair(entries.get(i).getRecordId(), distanceFromPoint));
                    }
                    i++;
                }
            }else{
                throw new IllegalStateException("Something went wrong when reading datafile");
            }
            blockId++;
        }
    }
}