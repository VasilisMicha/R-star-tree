import java.util.Arrays;

public class Point {
    private String id;
    private String name;
    private double[] coordinates;

    public Point(String id, String name, double[] coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Coordinates: " + Arrays.toString(coordinates);
    }
}
