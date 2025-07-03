/**
 * Implements a simple, linear range algorithm for points without using R*-Tree.
 * It filters points that are within a specified multidimensional range rectangle.
 *
 * @param points List of all available points to check.
 * @param rangeFilters Map with the limits per dimension.
 * @param queryDims Dimensions to be considered in the query.
 * @return List of points that are within the specified limits.
 **/

import java.util.*;

public class RangeQuery {
    public List<Point> execute(Map<String, double[]> rangeFilters, QueryDimensions queryDims) {
        List<Point> result = new ArrayList<>();
        long maxId = DataFileHandler.getMaxBlockId();
        for (int j=0; j<maxId; j++) {
            ArrayList<Point> points = DataFileHandler.readDataFileBlock(j + 1);
            for (Point point : points) {
                double[] coords = point.getCoordinates();
                boolean hasNaN = false;

                //Skip points with NaN values
                for (double val : coords) {
                    if (Double.isNaN(val)) {
                        hasNaN = true;
                        break;
                    }
                }
                if (hasNaN) continue;

                boolean matches = true;
                List<String> dimensions = queryDims.getDimensions();

                for (int i = 0; i < dimensions.size(); i++) {
                    String dim = dimensions.get(i);
                    if (rangeFilters.containsKey(dim)) {
                        double min = rangeFilters.get(dim)[0];
                        double max = rangeFilters.get(dim)[1];
                        if (coords[i] < min || coords[i] > max) {
                            matches = false;
                            break;
                        }
                    }
                }

                if (matches) {
                    result.add(point);
                }
            }
        }
        return result;
    }
}