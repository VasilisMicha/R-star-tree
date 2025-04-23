import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents a bound
 */
class Bounds implements Serializable{
    private double down;
    private double up;

    /**
     * Constructor
     * @param down
     * @param up
     */
    Bounds(double down, double up) {
        if (down<=up)
        {
            this.down=down;
            this.up=up;
        }
        else
            throw new IllegalArgumentException( "The lower value of the bounds cannot be bigger than the upper");
    }

    /**
     * Getter
     * @return down
     */
    double getDown() {
        return down;
    }

    /**
     * Getter
     * @return up
     */
    double getUp() {
        return up;
    }

    /**
     * Returns an ArrayList with bounds for each dimension
     * and the minimum bounds needed to fit the given entries
     * @param entries
     * @return
     */
    static ArrayList<Bounds> findMinimumBounds(ArrayList<Entry> entries){
        ArrayList<Bounds> minimumBounds= new ArrayList<>();
        for (int d=0; d<DataFileHandler.getDataDimensions(); d++){
            Entry downEntry= Collections.min(entries, new EntryComparator.EntryBoundComparator(entries,d,false));
            Entry upEntry= Collections.max(entries, new EntryComparator.EntryBoundComparator(entries,d,true));
            minimumBounds.add(new Bounds(downEntry.getBoundingBox().getBounds().get(d).getDown(),upEntry.getBoundingBox().getBounds().get(d).getUp()));
        }
        return minimumBounds;
    }

    /**
     * Returns an ArrayList with bounds for each dimension
     * and the minimum bounds needed to merge the given boxes
     * @param boundingBoxA
     * @param boundingBoxB
     * @return
     */
    static ArrayList<Bounds> findMinimumBounds(BoundingBox boundingBoxA, BoundingBox boundingBoxB) {
        ArrayList<Bounds> minimumBounds= new ArrayList<>();
        for (int d=0; d<DataFileHandler.getDataDimensions(); d++){
            double down= Math.min(boundingBoxA.getBounds().get(d).getDown(), boundingBoxB.getBounds().get(d).getDown());
            double up= Math.max(boundingBoxA.getBounds().get(d).getUp(), boundingBoxB.getBounds().get(d).getUp());
            minimumBounds.add(new Bounds(down,up));
        }
        return minimumBounds;
    }
}