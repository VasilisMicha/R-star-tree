/**
 * Implements the BNL (serial block nested loop) algorithm for finding Skyline.
 * @param points list of all points
 * @return list of non-dominated points
 * @author athinaanv
 **/

import java.util.ArrayList;
import java.util.List;

public class Skyline_BNL_Query {
    public static List<Point> computeSkyline(List<Point> points) {
        List<Point> skyline = new ArrayList<>(); //Skyline results

        for (Point p : points) {
            if (containsNaN(p)) continue; //Ignore NaN points

            boolean dominated = false; //Flag for dominated p
            List<Point> toRemove = new ArrayList<>(); //Points dominated by p

            for (Point q : skyline) {
                if (dominates(q, p)) {
                    dominated = true;
                    break;
                } else if (dominates(p, q)) {
                    toRemove.add(q);
                }
            }
            if (!dominated) {
                skyline.removeAll(toRemove);
                skyline.add(p);
            }
        }
        return skyline;
    }

    //Check if point a dominates point b
    private static boolean dominates(Point a, Point b) {
        double[] aCoords = a.getCoordinates();
        double[] bCoords = b.getCoordinates();

        boolean better = false; //Flag for the best point

        for (int i = 0; i < aCoords.length; i++) {
            if (aCoords[i] > bCoords[i]) return false;
            if (aCoords[i] < bCoords[i]) better = true;
        }
        return better;
    }

    //Check if a point contains NaN in some dimension
    private static boolean containsNaN(Point p) {
        for (double val : p.getCoordinates()) {
            if (Double.isNaN(val)) return true;
        }
        return false;
    }
}