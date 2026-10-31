
/**
 * Represents a spatial point with an ID, name, coordinates, and optional metadata like postcode and house number.
 * Supports construction from raw coordinates or a formatted string (e.g., CSV line).
 * Provides utility methods for distance calculation and conversion to a zero-area Rectangle.
 **/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

public class Point implements Serializable {
  private long id;
  private String name;
  private double[] coordinates;
  private int postcode;
  private int housenumber;
  private long blockId;

  // Constructor with coordinates only
  public Point(double[] coordinates) {
    this.id = 0;
    this.name = "";
    this.coordinates = coordinates.clone();
  }

  // Constructor with full initialization (id, name, coordinates)
  public Point(long id, String name, double[] coordinates) {
    this.id = id;
    this.name = name;
    this.coordinates = coordinates;
  }

  // Constructor parsing a formatted string (e.g., CSV line)
  public Point(String pointInString) {
    try {
      String[] data = pointInString.split(DataFileHandler.getDelimiter());
      if (data.length != 4 + DataFileHandler.dataDimensions)
        throw new IllegalStateException("The data on csv are not what are expected");

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

  // Returns a string representation of the point
  @Override
  public String toString() {
    return "ID: " + id + ", Name: " + name + ", Coordinates: " + Arrays.toString(coordinates);
  }

  // Converts this Point to a zero-area Rectangle at the same coordinates
  public Rectangle toRectangle() {
    return new Rectangle(coordinates.clone(), coordinates.clone());
  }

  // Returns a defensive copy of the coordinates
  public double[] getCoordinates() {
    return coordinates;
  }

  // Calculates Euclidean distance between this point and another
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

  // Setters/getters for metadata fields
  public void setName(String name) {
    this.name = name;
  }

  public void setBlockId(long blockId) {
    this.blockId = blockId;
  }

  public String getName() {
    return name;
  }

  public int getPostcode() {
    return postcode;
  }

  public int getHousenumber() {
    return housenumber;
  }

  public long getBlockId() {
    return blockId;
  }

  public long getId() {
    return id;
  }

  // Returns coordinates as a list of Doubles
  public ArrayList<Double> getCoordinatesAsList() {
    ArrayList<Double> coords = new ArrayList<>();
    for (double d : coordinates) {
      coords.add(d);
    }
    return coords;
  }

  public long getId() {
    return id;
  }
}
