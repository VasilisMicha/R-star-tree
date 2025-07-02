/**
 * Represents an axis-aligned hyperrectangle in N-dimensional space,
 * defined by min and max coordinate boundaries.
 * Supports geometric operations such as distance calculation,
 * intersection testing, containment checks, enlargement,
 * and overlap measurement.
 **/

import java.io.Serializable;

public class Rectangle implements Serializable {
    private final double[] min;
    private final double[] max;
    private String name;
    private long id;

    public long getId(){ return id; }
    public String getName(){ return name; }

    //Constructor with ID and name metadata
    public Rectangle(double[] min, double[] max, long id, String name) {
        this.min = min.clone();
        this.max = max.clone();
        this.id = id;
        this.name = name;
    }

    //Constructor that verifies dimensionality
    public Rectangle(double[] min, double[] max) {
        if (min.length != max.length)
            throw new IllegalArgumentException("Dimensions mismatch.");
        this.min = min.clone();
        this.max = max.clone();
    }

    //String representation for debugging
    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Min: " + min + ", Max: " + max;
    }

    //Returns center point of the rectangle
    public Point getCenter() {
        double[] coords = new double[min.length];
        for (int i = 0; i < min.length; i++) {
            coords[i] = (min[i] + max[i]) / 2;
        }
        return new Point(coords);
    }

    //Euclidean distance from rectangle center to a point
    public double distanceTo(Point p) {
        double sum = 0;
        double[] centerCoords = getCenter().getCoordinates();
        double[] pCoords = p.getCoordinates();
        for (int i = 0; i < min.length; i++) {
            double diff = centerCoords[i] - pCoords[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    //Calculates the hypervolume (area in n-D)
    public double calculateArea() {
        double area = 1.0;
        for (int i = 0; i < min.length; i++) {
            area *= max[i] - min[i];
        }
        return area;
    }

    //Checks if this rectangle intersects with another
    public boolean intersects(Rectangle other) {
        for (int i = 0; i < min.length; i++) {
            if (this.max[i] < other.min[i] || this.min[i] > other.max[i]) {
                return false;
            }
        }
        return true;
    }

    //Checks if this rectangle contains a given point
    public boolean contains(Point p) {
        double[] coords = p.getCoordinates();
        for (int i = 0; i < min.length; i++) {
            if (coords[i] < min[i] || coords[i] > max[i]) {
                return false;
            }
        }
        return true;
    }

    //Returns minimal rectangle enclosing this and another
    public Rectangle enlarge(Rectangle other) {
        double[] newMin = new double[min.length];
        double[] newMax = new double[max.length];
        for (int i = 0; i < min.length; i++) {
            newMin[i] = Math.min(min[i], other.min[i]);
            newMax[i] = Math.max(max[i], other.max[i]);
        }
        return new Rectangle(newMin, newMax);
    }

    //Calculates the area increase if enlarged to include "other"
    public double calculateAreaEnlargement(Rectangle other) {
        return this.enlarge(other).calculateArea() - this.calculateArea();
    }

    //Calculates overlapping volume
    public double calculateOverlap(Rectangle other) {
        double overlap = 1.0;
        for (int i = 0; i < min.length; i++) {
            double maxMin = Math.max(min[i], other.min[i]);
            double minMax = Math.min(max[i], other.max[i]);
            if (maxMin >= minMax) return 0.0;
            overlap *= (minMax - maxMin);
        }
        return overlap;
    }

    //Sum of edge lengths
    public double calculateMargin() {
        double margin = 0.0;
        for (int i = 0; i < min.length; i++) {
            margin += (max[i] - min[i]);
        }
        return margin;
    }

    public double[] getMin() { return min.clone(); }
    public double[] getMax() { return max.clone(); }

    //Min Euclidean distance from rectangle to point
    public double minDistanceTo(Point p) {
        double sum = 0.0;
        double[] coords = p.getCoordinates();
        for (int i = 0; i < min.length; i++) {
            if (coords[i] < min[i]) {
                sum += Math.pow(min[i] - coords[i], 2);
            } else if (coords[i] > max[i]) {
                sum += Math.pow(coords[i] - max[i], 2);
            }
        }
        return Math.sqrt(sum);
    }

    //Checks containment from raw coords
    public boolean contains(double[] coords) {
        return contains(new Point(coords));
    }
}
