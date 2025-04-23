/**
* This class implements a simple range query
* (finding points within a rectangular range) without using R-Tree
* to check a dataset of points.

* @param points list of points
* @param minCoords minimum values per dimension (2D, 3D, etc.)
* @param maxCoords maximum values per dimension
* @return list of points that are within the area
* * @author athinaanv
**/

import java.util.ArrayList;
import java.util.List;

public class RangeQuery {
    public static List<Point> execute(List<Point> points, double[] minCoords, double[] maxCoords) {
        List<Point> result = new ArrayList<>();

        for (Point point : points) {
            double[] coords = point.getCoordinates();
            boolean inside = true;

            for (int i = 0; i < coords.length; i++) {
                if (coords[i] < minCoords[i] || coords[i] > maxCoords[i]) {
                    inside = false;
                    break;
                }
            }

            if (inside) {
                result.add(point);
            }
        }

        return result;
    }

    //Method test
    public static void main(String[] args) {

        String filePath = "src/dbtechnology/data.csv";
        List<Point> points = DataParser.parsePointsAutoDetect(filePath);

        //Area definition
        double[] min = {};
        double[] max = {};


        List<Point> inRange = execute(points, min, max);
        System.out.println("Found " + inRange.size() + " points within the range:");
        for (Point p : inRange) {
            System.out.println(p);
        }
    }
}

