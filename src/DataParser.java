/**
 Class for parsing data from CSV files and converting them to Point objects.
 Supports automatic detection of coordinate columns based on headers.

 * Reads a CSV file and returns a list of points
 * @param filePath Path to the CSV file
 * @param queryDims The column dimensions to be exported as coordinates
 * @return List of Point objects
 * @author athinaanv
 **/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;

public class DataParser {
    public static List<Point> parsePointsAutoDetect(String filePath, QueryDimensions queryDims) throws IOException {
        List<Point> points = new ArrayList<>();  //List to store points

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            //Read the column headings
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IOException("File is empty or the header is missing.");
            }

            //Separate the headings and create a map
            String[] headers = headerLine.split(",");
            Map<String, Integer> headerIndexMap = buildHeaderIndexMap(headers);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] tokens = line.split(",");
                if (tokens.length < 2) continue;  //Min required columns: id, name

                long id = parseLong(tokens[0]);
                String name = tokens[1];

                double[] coords = extractCoordinates(tokens, queryDims, headerIndexMap);

                points.add(new Point(id, name, coords));  //Create and add point
            }
        }
        return points;
    }

    //Μatch column names with indexes in a map
    private static Map<String, Integer> buildHeaderIndexMap(String[] headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            map.put(headers[i].trim(), i);
        }
        return map;
    }

    private static double[] extractCoordinates(String[] tokens, QueryDimensions queryDims,
                                               Map<String, Integer> headerIndexMap) {
        double[] coords = new double[queryDims.size()];  //Table for coordinates

        for (int i = 0; i < queryDims.size(); i++) {
            String dim = queryDims.getDimensions().get(i);
            Integer colIndex = headerIndexMap.get(dim);

            if (colIndex != null && colIndex < tokens.length) {
                try {
                    coords[i] = Double.parseDouble(tokens[colIndex]);
                } catch (NumberFormatException e) {
                    coords[i] = Double.NaN;  //Invalid value
                }
            } else {
                coords[i] = Double.NaN;      //Column or value is missing
            }
        }
        return coords;
    }
}