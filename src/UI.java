import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class UI {
    RStarTree tree;

    public UI(){
        DataFileHandler.initializeDataFile(2, false);
        tree = new RStarTree();
        tree.bulkLoad();
    }

    public RStarTree getTree(){
        return tree;
    }

    public void KNN(int k){
        ArrayList<Point> points = new ArrayList<>();
        long maxId = DataFileHandler.getMaxBlockId();
        for (int j=0; j<maxId; j++) {
            ArrayList<Point> blockPoints = DataFileHandler.readDataFileBlock(j + 1);
            for (Point point : blockPoints) {
                points.add(point);
            }
        }
        long startTime = System.nanoTime();

        NearestNeighboursQuery knn = new NearestNeighboursQuery(new Point(new double[] {41.8658994, 12.53403}), k);
        ArrayList<Point> queryPoints = (ArrayList<Point>)knn.getQueryRecordIds()
                .stream()
                .map(id -> points.stream().filter(p -> p.getId() == id).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        long endTime = System.nanoTime();  // end timer
        long duration = endTime - startTime;  // in nanoseconds
        for (Point point : queryPoints){
            System.out.println(point.getCoordinates()[0]+"-"+point.getCoordinates()[1]);
        }
        System.out.println("Time taken: " + (duration / 1_000_000.0) + " ms");
    }

    public void linearKNN(int k){
        long startTime = System.nanoTime();

        LinearSearchNearestNeighbours lknn = new LinearSearchNearestNeighbours();
        ArrayList<Point> points = (ArrayList<Point>)lknn.findKNearestNeighbors(new Point(new double[] {41.8658994, 12.53403}), k);

        long endTime = System.nanoTime();  // end timer
        long duration = endTime - startTime;  // in nanoseconds
        System.out.println("Time taken: " + (duration / 1_000_000.0) + " ms");
        for (Point point : points){
            System.out.println(point.getCoordinates()[0]+"-"+point.getCoordinates()[1]);
        }
    }

    public void rangeQuery(){
        Rectangle[] testAreas = {
                new Rectangle(new double[]{41.8756791, 12.3954822}, new double[]{41.8847698, 12.4695661}),  // 1166
                new Rectangle(new double[]{41.8560068, 12.4654822}, new double[]{41.9147698, 12.5695661}),  // 2463
                new Rectangle(new double[]{41.8656791, 12.5335863}, new double[]{41.8660624, 12.5339965})   // 11
        };
        long startTime = System.nanoTime();

        RStarTreeRangeQuery rq = new RStarTreeRangeQuery(tree);
        ArrayList<Point> points = (ArrayList<Point>) rq.execute(testAreas[1]);

        long endTime = System.nanoTime();  // end timer
        long duration = endTime - startTime;  // in nanoseconds
        for (Point point : points){
            System.out.println(point.getCoordinates()[0]+"-"+point.getCoordinates()[1]);
        }
        System.out.println("Time taken: " + (duration / 1_000_000.0) + " ms");
        System.out.println("result count: "+points.size());
    }

    public void linearRangeQuery(int i){
        Rectangle[] testAreas = {
                new Rectangle(new double[]{41.8656791, 12.3654822}, new double[]{41.8947698, 12.4695661}),  // 1166
                new Rectangle(new double[]{41.7560068, 12.3654822}, new double[]{41.9147698, 12.6695661}),  // 2463
                new Rectangle(new double[]{41.8656791, 12.5335863}, new double[]{41.8660624, 12.5339965})   // 11
        };
        long startTime = System.nanoTime();

        RangeQuery rq = new RangeQuery();
        ArrayList<Point> points = (ArrayList<Point>) rq.execute(Map.of(
                "lat", new double[]{testAreas[i].getMin()[0], testAreas[i].getMax()[0]},//41.8660624-12.5335863
                "lon", new double[]{testAreas[i].getMin()[1], testAreas[i].getMax()[1]}//41.8656791-12.5339965
        ), new QueryDimensions("lat", "lon"));

        long endTime = System.nanoTime();  // end timer
        long duration = endTime - startTime;  // in nanoseconds
        for (Point point : points){
            System.out.println(point.getCoordinates()[0]+"-"+point.getCoordinates()[1]);
        }
        System.out.println("Time taken: " + (duration / 1_000_000.0) + " ms");
        System.out.println("result count: "+points.size());
    }

    public void skyline(){
        long startTime = System.nanoTime();

        ArrayList<Point> points = (ArrayList<Point>) Skyline_BBS_Query.computeSkyline(tree);

        long endTime = System.nanoTime();  // end timer
        long duration = endTime - startTime;  // in nanoseconds
        for (Point point : points){
            System.out.println(point.getCoordinates()[0]+"-"+point.getCoordinates()[1]);
        }
        System.out.println("Time taken: " + (duration / 1_000_000.0) + " ms");
        System.out.println("result count: "+points.size());
    }

    public void linearSkyline(){
        long startTime = System.nanoTime();

        ArrayList<Point> skylinePoints = (ArrayList<Point>) Skyline_BNL_Query.computeSkyline(points());

        long endTime = System.nanoTime();  // end timer
        long duration = endTime - startTime;  // in nanoseconds
        for (Point point : skylinePoints){
            System.out.println(point.getCoordinates()[0]+"-"+point.getCoordinates()[1]);
        }
        System.out.println("Time taken: " + (duration / 1_000_000.0) + " ms");
        System.out.println("result count: "+skylinePoints.size());
    }

    public ArrayList<Point> points(){
        ArrayList<Point> points = new ArrayList<>();
        long maxId = DataFileHandler.getMaxBlockId();
        for (int j=0; j<maxId; j++) {
            ArrayList<Point> blockPoints = DataFileHandler.readDataFileBlock(j + 1);
            for (Point point : blockPoints) {
                points.add(point);
                System.out.println(point.getCoordinates()[0]+"-"+point.getCoordinates()[1]);
            }
        }
        return points;
    }

}
