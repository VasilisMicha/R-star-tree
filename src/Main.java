import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
//        QueryDimensions queryDims = new QueryDimensions("lat", "lon");
//        String filePath = "src/data2.csv";
//        List<Point> points = DataParser.parsePointsAutoDetect(filePath, queryDims);
//        System.out.println("Total points loaded: " + points.size());
//        IndexFileHandler.initializeIndexFile(queryDims.size(), true);
//        RStarTree tree = new RStarTree();
//        for (Point p : points) { tree.insert(p);}
//        Map<String, double[]> rangeFilters = new HashMap<>();
//
//        //Linear Search Example KNN
//        Point queryPoint = new Point("query", "QueryPoint", new double[]{4.0, 5.0});
//        LinearSearchNearestNeighbours linearSearch = new LinearSearchNearestNeighbours(points);
//        List<Point> linearResults = linearSearch.findKNearestNeighbors(queryPoint, 3);
//        System.out.println("Linear Search Results KNN (3 nearest neighbors):");
//        for (Point p : linearResults) {
//            System.out.println(p.getId() + " - " + p.getName()+" Distance: " + p.distanceTo(queryPoint));
//        }
//
//        //R*-tree KNN Query Example
//        NearestNeighboursQuery treeQuery = new NearestNeighboursQuery(queryPoint, 3);
//        ArrayList<Long> treeResults = treeQuery.getQueryRecordIds();
//        System.out.println("\nR*-tree Query Results (3 nearest neighbors):");
//        for (Long id : treeResults) {
//            // Κάπως να βάλω να εμφανίζονται και τα στοιχεία του κάθε Point
//            System.out.println("Record ID: " + id);
//        }
        DataFileHandler.initializeDataFile(2, true);
        System.out.println(DataFileHandler.calculateMaxPointsInBlock());
        File dataFile = new File("src//datafile.dat");
        System.out.println(dataFile.length()/(32*1024));
    }
}