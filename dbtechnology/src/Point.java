/**
 * This class represents a point in multidimensional space with a unique identifier (id), name, and coordinate array.
 * Used to store and search data in R*-Tree.
 * @author VasilisMicha, athinaanv
**/

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

    public double[] getCoordinates() {
        return coordinates;
    }

    public int getSize(){
        return (Integer.SIZE / 8) + coordinates.length * (Double.SIZE / 8);
    }

}