import java.io.Serializable;

public class Rectangle implements Serializable {
    private final double[] min;
    private final double[] max;

    public Rectangle(double[] min, double[] max) {
        if (min.length != max.length)
            throw new IllegalArgumentException("Dimensions mismatch.");
        this.min = min.clone();
        this.max = max.clone();
    }

    public Point getCenter() {
        double[] coords = new double[min.length];
        for (int i = 0; i < min.length; i++) {
            coords[i] = (min[i] + max[i]) / 2;
        }
        return new Point(coords);
    }

    public double distanceTo(Point p) {
        double sum = 0;
        for (int i = 0; i < min.length; i++) {
            double diff = getCenter().getCoords()[i] - p.getCoords()[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /** Calculates the hypervolume (area in n-D). */
    public double calculateArea() {
        double area = 1.0;
        for (int i = 0; i < min.length; i++) {
            area *= max[i] - min[i];
        }
        return area;
    }

    /** Returns the minimal rectangle enclosing this and another rectangle. */
    public Rectangle enlarge(Rectangle other) {
        double[] newMin = new double[min.length];
        double[] newMax = new double[max.length];
        for (int i = 0; i < min.length; i++) {
            newMin[i] = Math.min(min[i], other.min[i]);
            newMax[i] = Math.max(max[i], other.max[i]);
        }
        return new Rectangle(newMin, newMax);
    }

    /** Calculates the area increase if enlarged to include 'other'. */
    public double calculateAreaEnlargement(Rectangle other) {
        return this.enlarge(other).calculateArea() - this.calculateArea();
    }

    /** Calculates the overlapping hypervolume with another rectangle. */
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

    public double calculateMargin() {
        double margin = 0.0;
        for (int i = 0; i < min.length; i++) {
            margin += (max[i] - min[i]);
        }
        return margin;
    }

    public double[] getMin() { return min.clone(); }
    public double[] getMax() { return max.clone(); }
}