import java.io.Serializable;
import java.util.ArrayList;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 * Represents a bounding box in the n-dimensional space
 */
class BoundingBox implements Serializable{
    private ArrayList<Bounds> bounds;
    private Double area;
    private Double margin;
    private ArrayList<Double> center;

    /**
     * Constructor
     * @param bounds
     */
    BoundingBox(ArrayList<Bounds> bounds){
        this.bounds= bounds;
        this.area= calculateArea();
        this.margin= calculateMargin();
        this.center= getCenter();
    }

    /**
     * Getter
     * @return an ArrayList that has bounds
     */
    ArrayList<Bounds> getBounds(){
        return bounds;
    }

    /**
     * Getter
     * @return area
     */
    double getArea(){
        if (area == null){
            area= calculateArea();
        }
        return area;
    }

    /**
     * Getter
     * @return margin
     */
    double getMargin(){
        if (margin == null){
            margin= calculateMargin();
        }
        return margin;
    }

    /**
     * Return the minimmum distance between the bounding box and the point
     * @param point
     * @return
     */
    double findMinDistanceFromPoint(ArrayList<Double> point){
        double minDistance=0;
        double rd;
        for (int i=0; i<DataFileHandler.getDataDimensions(); i++){
            if(getBounds().get(i).getDown()> point.get(i)) {
                rd = getBounds().get(i).getDown();
            }else if (getBounds().get(i).getUp() < point.get(i)) {
                rd = getBounds().get(i).getUp();
            }else {
                rd = point.get(i);
            }
            minDistance += Math.pow(point.get(i) - rd,2);
        }
        return sqrt(minDistance);
    }

    /**
     * Getter
     * @return center
     */
    private ArrayList<Double> getCenter(){
        if (center == null){
            center = new ArrayList<>();
            for (int d = 0; d < DataFileHandler.getDataDimensions(); d++) {
                center.add((bounds.get(d).getUp() + bounds.get(d).getDown()) / 2);
            }
        }
        return center;
    }

    /**
     * Calculates and return the perimeter of the this bounding box
     * @return margin
     */
    private double calculateMargin() {
        double sum= 0;
        for (int d=0;d<DataFileHandler.getDataDimensions();d++) {
            sum += abs(bounds.get(d).getUp()- bounds.get(d).getDown());
        }
        return sum;
    }

    /**
     * Calculates and returns the area of this bounding box
     * @return area
     */
    private double calculateArea() {
        double area1= 1;
        for (int d=0;d<DataFileHandler.getDataDimensions(); d++)
            area1 *= (bounds.get(d).getUp()- bounds.get(d).getDown());
        return abs(area1);
    }

    /**
     * Returns true if the two bounding boxes overlap
     * @param boundingBoxA
     * @param boundingBoxB
     * @return
     */
    static boolean checkOverlap(BoundingBox boundingBoxA, BoundingBox boundingBoxB) {
        // For every dimension find the intersection point
        for (int d=0;d<DataFileHandler.getDataDimensions(); d++){
            double overlapD = Math.min(boundingBoxA.getBounds().get(d).getUp(), boundingBoxB.getBounds().get(d).getUp())
                    - Math.max(boundingBoxA.getBounds().get(d).getDown(), boundingBoxB.getBounds().get(d).getDown());
            if (overlapD < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the overlap value between two bounding boxes
     * @param boundingBoxA
     * @param boundingBoxB
     * @return
     */
    static double calculateOverlapValue(BoundingBox boundingBoxA, BoundingBox boundingBoxB) {
        double overlapValue= 1;
        for (int i=0;i<DataFileHandler.getDataDimensions(); i++){
            double overlapD= Math.min(boundingBoxA.getBounds().get(i).getUp(), boundingBoxB.getBounds().get(i).getUp())
                    - Math.max(boundingBoxA.getBounds().get(i).getDown(), boundingBoxB.getBounds().get(i).getDown());
            if (overlapD <= 0){
                return 0;
            }else{
                overlapValue= overlapD * overlapValue;
            }
        }
        return overlapValue;
    }

    /**
     * Returns the euclidean distance between two bounding boxes's centers
     * @param boundingBoxA
     * @param boundingBoxB
     * @return
     */
    static double findDistanceBetweenBoundingBoxes(BoundingBox boundingBoxA, BoundingBox boundingBoxB){
        double distance=0;
        for (int i=0;i<DataFileHandler.getDataDimensions(); i++){
            distance +=Math.pow(boundingBoxA.getCenter().get(i)- boundingBoxB.getCenter().get(i),2);
        }
        return sqrt(distance);
    }
}