import java.util.Arrays;

public class Point {
    private String id;
    private String name;
    private final double[] coordinates;

    public Point(String id, String name, double[] coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Coordinates: " + Arrays.toString(coordinates);
    }

    /**
     * Converts this Point to a Rectangle with the same coordinates, forming a zero-area hyperrectangle.
     */
    public Rectangle toRectangle() {
        return new Rectangle(coordinates.clone(), coordinates.clone(), id, name);
    }


    public double[] getCoordinates() {
        return coordinates.clone();
    }

    public int getSize(){
        return (Integer.SIZE / 8) + coordinates.length * (Double.SIZE / 8);
    }

    public Point(double[] coordinates) {
        this.id = "";   // ή null, ή κάποιο default id
        this.name = ""; // ή null, ή κάποιο default όνομα
        this.coordinates = coordinates.clone();
    }

}