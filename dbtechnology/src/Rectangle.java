/**
 * Represents a multidimensional rectangle (MBR - Minimum Bounding Rectangle).
 * Used to describe spatial boundaries of nodes or points in the R*-Tree.
 * Provides methods for checking intersection and inclusion with other rectangles or points.
 * @author athinaanv
 **/

import java.io.Serializable;
import java.util.Arrays;

class Rectangle implements Serializable {
    private final double[] min;
    private final double[] max;

    public Rectangle(double[] min, double[] max) {
        this.min = min;
        this.max = max;
    }

    public double[] getMin() {
        return min;
    }

    public double[] getMax() {
        return max;
    }

    public boolean intersects(Rectangle other) {
        for (int i = 0; i < min.length; i++) {
            if (this.max[i] < other.min[i] || this.min[i] > other.max[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(Point point) {
        double[] coords = point.getCoordinates();
        for (int i = 0; i < coords.length; i++) {
            if (coords[i] < min[i] || coords[i] > max[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Rectangle{" +
                "min=" + Arrays.toString(min) +
                ", max=" + Arrays.toString(max) +
                '}';
    }
}