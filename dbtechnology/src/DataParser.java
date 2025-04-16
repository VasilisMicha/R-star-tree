/**
 * This class handles splitting the data into records, reading the file, and creating points from coordinates.
 * It Reads a CSV file and returns a list of point objects.
 * The method supports dynamic column detection without requiring a fixed structure.
 *
 * @param filePath the CSV file path
 * @return a list of point entries
 *  @author athinaanv
 **/

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DataParser {

    public static List<Point> parsePointsAutoDetect(String filePath) {
        List<Point> points = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true; //Check for the header

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] tokens = line.split(",");

                //Dimension calculation (based on the column number)
                int dimensions = tokens.length - 2;
                if (dimensions < 1) {
                    System.err.println("Error: Missing line: " + line);
                    continue;
                }

                String id = tokens[0]; //First column (prob ID)
                String name = tokens[1]; //Second column (prob  NAME)
                double[] coords = new double[dimensions];

                //Conversion of geographic coordinates
                for (int i = 0; i < dimensions; i++) {
                    try {
                        coords[i] = Double.parseDouble(tokens[i + 2]);
                    } catch (NumberFormatException e) {
                        System.err.println("Error converting to number: " + tokens[i + 2] + " on the line: " + line);
                        coords[i] = Double.NaN;
                    }
                }

                //Create and add the point object to the list
                points.add(new Point(id, name, coords));
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return points;
    }


     //Method test
    public static void main(String[] args) {
        //Set the path to the file
        Path path = Paths.get("src/dbtechnology/data2.csv");
        String filePath = path.toString();

        List<Point> points = parsePointsAutoDetect(filePath);

        System.out.println("Points: " + points.size());
        for (Point point : points) {
            System.out.println(point);
        }
    }
}
