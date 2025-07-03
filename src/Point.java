import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class Point implements Serializable {
    private long id;
    private String name;
    private double[] coordinates;
    private int postcode;
    private int housenumber;
    private long blockId;


    public Point(double[] coordinates) {
        this.id = 0;   // ή null, ή κάποιο default id
        this.name = ""; // ή null, ή κάποιο default όνομα
        this.coordinates = coordinates.clone();
    }

    public Point(long id, String name, double[] coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }

    public Point(String pointInString){
        try {
            String[] data = pointInString.split(DataFileHandler.getDelimiter());
            if (data.length != 4 + DataFileHandler.dataDimensions) return;
            this.id = parseLong(data[0]);
            this.name = data[1];
            coordinates = new double[DataFileHandler.dataDimensions];
            for (int i = 0; i < DataFileHandler.dataDimensions; i++) {
                coordinates[i] = parseDouble(data[i + 2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Coordinates: " + Arrays.toString(coordinates);
    }

    /**
     * Converts this Point to a Rectangle with the same coordinates, forming a zero-area hyperrectangle.
     */
    public Rectangle toRectangle() {
        return new Rectangle(coordinates.clone(), coordinates.clone());
    }


    public double[] getCoordinates() {
        return coordinates;
    }

    public double distanceTo(Point other) {
        if (this.coordinates.length != other.coordinates.length) {
            throw new IllegalArgumentException("Points must have same dimensions");
        }

        double sum = 0.0;
        for (int i = 0; i < this.coordinates.length; i++) {
            double diff = this.coordinates[i] - other.coordinates[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    public void setName(String name){
        this.name = name;
    }

    public void setBlockId(long blockId){
        this.blockId = blockId;
    }

    public String getName(){
        return name;
    }

    public int getPostcode(){
        return postcode;
    }

    public int getHousenumber(){
        return housenumber;
    }

    public long getBlockId(){
        return blockId;
    }
    public long getId(){return id;}

    public ArrayList<Double> getCoordinatesAsList() {
        ArrayList<Double> coords = new ArrayList<>();
        for (double d : coordinates) {
            coords.add(d);
        }
        return coords;
    }

}
