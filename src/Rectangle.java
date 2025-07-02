import java.io.Serializable;

public class Rectangle implements Serializable {
    private final double[] min;
    private final double[] max;
    private String name;
    private long id;
    public long getId(){ return id; }
    public String getName(){ return name; }

    public Rectangle(double[] min, double[] max, long id, String name) {
        this.min =min.clone();
        this.max =max.clone();
        this.id = id;
        this.name = name;
    }
    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Min: " + min + ", Max: " + max;
    }

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
            double diff = getCenter().getCoordinates()[i] - p.getCoordinates()[i];
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

    public boolean intersects(Rectangle other) {
        for (int i = 0; i < min.length; i++) {
            if (this.max[i] < other.min[i] || this.min[i] > other.max[i]) {
                return false; // Δεν υπάρχει επικάλυψη στη διάσταση i
            }
        }
        return true; // Υπάρχει επικάλυψη σε όλες τις διαστάσεις
    }

    public boolean contains(Point p) {
        double[] coords = p.getCoordinates();
        for (int i = 0; i < min.length; i++) {
            if (coords[i] < min[i] || coords[i] > max[i]) {
                return false; // Το σημείο είναι έξω από τα όρια σε κάποια διάσταση
            }
        }
        return true; // Το σημείο είναι μέσα στο ορθογώνιο σε όλες τις διαστάσεις
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

    public double minDistanceTo(Point p) {
        double sum = 0.0;
        for (int i = 0; i < min.length; i++) {
            double coord = p.getCoordinates()[i];
            if (coord < min[i]) {
                sum += Math.pow(min[i] - coord, 2);
            } else if (coord > max[i]) {
                sum += Math.pow(coord - max[i], 2);
            }
            // else coord is within bounds on this dimension
        }
        return Math.sqrt(sum);
    }

    public boolean contains(double[] coords) {
        return contains(new Point(coords));
    }
}