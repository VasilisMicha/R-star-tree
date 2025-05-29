import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        QueryDimensions queryDims = new QueryDimensions("lat", "lon");
        String filePath = "src/data2.csv";
        List<Point> points = DataParser.parsePointsAutoDetect(filePath, queryDims);
        System.out.println("Total points loaded: " + points.size());
        IndexFileHandler.initializeIndexFile(queryDims.size(), true);
        RStarTree tree = new RStarTree();
        for (Point p : points) { tree.insert(p);}
        Map<String, double[]> rangeFilters = new HashMap<>();

        //Linear Search Example KNN
        Point queryPoint = new Point("query", "QueryPoint", new double[]{4.0, 5.0});
        LinearSearchNearestNeighbours linearSearch = new LinearSearchNearestNeighbours(points);
        List<Point> linearResults = linearSearch.findKNearestNeighbors(queryPoint, 3);
        System.out.println("Linear Search Results KNN (3 nearest neighbors):");
        for (Point p : linearResults) {
            System.out.println(p.getId() + " - " + p.getName()+" Distance: " + p.distanceTo(queryPoint));
        }

        //R*-tree KNN Query Example
        NearestNeighboursQuery treeQuery = new NearestNeighboursQuery(queryPoint, 3);
        ArrayList<Long> treeResults = treeQuery.getQueryRecordIds();
        System.out.println("\nR*-tree Query Results (3 nearest neighbors):");
        for (Long id : treeResults) {
            // Κάπως να βάλω να εμφανίζονται και τα στοιχεία του κάθε Point
            System.out.println("Record ID: " + id);
        }
    }
}

/*
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        QueryDimensions queryDims = new QueryDimensions("lat", "lon");
        String filePath = "src/data2.csv";
        List<Point> points = DataParser.parsePointsAutoDetect(filePath, queryDims);
        System.out.println("Total points loaded: " + points.size());
        IndexFileHandler.initializeIndexFile(queryDims.size(), true);
        RStarTree tree = new RStarTree();
        for (Point p : points) {
            tree.insert(p);
        }
        Map<String, double[]> rangeFilters = new HashMap<>();
        Map<Long, Point> idToPointMap = new HashMap<>();
        for (Point p : points) {
            long blockId = // get blockId από το Point;
            idToPointMap.put(blockId, p);
        }

        // Linear Search Example KNN
        Point queryPoint = new Point("query", "QueryPoint", new double[]{4.0, 5.0});
        LinearSearchNearestNeighbours linearSearch = new LinearSearchNearestNeighbours(points);
        List<Point> linearResults = linearSearch.findKNearestNeighbors(queryPoint, 3);
        System.out.println("\nLinear Search Results KNN (3 nearest neighbors):");
        for (Point p : linearResults) {
            System.out.println(p.getId() + " - " + p.getName() +
                    " Distance: " + p.distanceTo(queryPoint));
        }

        // R*-tree KNN Query Example
        NearestNeighboursQuery treeQuery = new NearestNeighboursQuery(queryPoint, 3);
        ArrayList<Long> treeResults = treeQuery.getQueryRecordIds();
        System.out.println("\nR*-tree Query Results (3 nearest neighbors):");
        for (Long id : treeResults) {
            Point p = idToPointMap.get(id);
            if (p != null) {
                System.out.println(p.getId() + " - " + p.getName() +
                        " Distance: " + p.distanceTo(queryPoint));
            } else {
                System.out.println("Record ID: " + id + " (Point not found in map)");
            }
        }
    }
}
 */