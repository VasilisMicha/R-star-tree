import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class that represents a point in a n-dimensional space that has a unique id (record)
 */
class Point implements Serializable {
    private long id;
    private ArrayList<Double> coords;
    private String name;

    /**
     * Constructor for Point class
     * @param id1
     * @param coords1
     * @param name1
     */
    public Point(long id1, ArrayList<Double> coords1, String name1) {
        id = id1;
        coords = coords1;
        name = name1;
    }

    /**
     * Constructor for Point with only coordinates (useful for center points)
     * @param coords array of coordinates
     */
    public Point(double[] coords) {
        this.id = -1; // indicates no id
        this.coords = new ArrayList<>();
        for (double coord : coords) {
            this.coords.add(coord);
        }
        this.name = "";
    }

    /**
     * Given string will be split by the delimiter provided
     * @param pointInString
     */
    public Point(String pointInString) {
        String[] stringArray;
        stringArray = pointInString.split(DataFileHandler.getDelimiter());
        int len = stringArray.length;
        if (len != DataFileHandler.getDataDimensions() + 1) {
            throw new IllegalArgumentException("Wrong dimensions");
        }
        this.id = Long.parseLong(stringArray[0]);
        this.name = stringArray[1];
        this.coords = new ArrayList<>();
        for (int i = 2; i < len; i++)
            coords.add(Double.parseDouble(stringArray[i]));
    }

    /**
     * Getter for id
     * @return id
     */
    long getId() {
        return id;
    }

    /**
     * Returns the coordinate based on the dimension given as parameter
     * @param dimension
     * @return the coordinate at specified dimension
     */
    double getCoords(int dimension) {
        return coords.get(dimension);
    }

    /**
     * Get all coordinates as array
     * @return array of coordinates
     */
    double[] getCoords() {
        double[] coordArray = new double[coords.size()];
        for (int i = 0; i < coords.size(); i++) {
            coordArray[i] = coords.get(i);
        }
        return coordArray;
    }

    /**
     * Getter for name
     * @return name
     */
    String getName() {
        return name;
    }

    /**
     * Checks if this point is inside the given rectangle
     * @param rect the rectangle to check
     * @return true if point is inside the rectangle
     */
    public boolean isInside(Rectangle rect) {
        double[] min = rect.getMin();
        double[] max = rect.getMax();
        for (int i = 0; i < coords.size(); i++) {
            if (coords.get(i) < min[i] || coords.get(i) > max[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the distance to another point
     * @param other the other point
     * @return Euclidean distance between the points
     */
    public double distanceTo(Point other) {
        double sum = 0;
        for (int i = 0; i < coords.size(); i++) {
            double diff = coords.get(i) - other.getCoords(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * method that creates a string based on a record
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder pointToString = new StringBuilder(id + "," + name);
        for (Double coord : coords) {
            pointToString.append(",").append(coord);
        }
        return pointToString.toString();
    }
}