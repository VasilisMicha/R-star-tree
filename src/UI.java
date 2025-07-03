import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class UI {
    RStarTree tree;

    public UI(){
        DataFileHandler.initializeDataFile(2, true);
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
        System.out.println("Time taken: " + (duration / 1_000_000_000.0) + " sec");
    }

    public void linearKNN(int k){
        long startTime = System.nanoTime();

        LinearSearchNearestNeighbours lknn = new LinearSearchNearestNeighbours();
        ArrayList<Point> points = (ArrayList<Point>)lknn.findKNearestNeighbors(new Point(new double[] {41.8658994, 12.53403}), k);

        long endTime = System.nanoTime();  // end timer
        long duration = endTime - startTime;  // in nanoseconds
        System.out.println("Time taken: " + (duration / 1_000_000_000.0) + " sec");
        for (Point point : points){
            System.out.println(point.getCoordinates()[0]+"-"+point.getCoordinates()[1]);
        }
    }

    public void rangeQuery(){
        long startTime = System.nanoTime();

        RStarTreeRangeQuery rq = new RStarTreeRangeQuery(tree);
        ArrayList<Point> points = (ArrayList<Point>) rq.execute(new Rectangle(new double[]{41.8656791, 12.5335863}, new double[]{41.8660624, 12.5339965} ));

        long endTime = System.nanoTime();  // end timer
        long duration = endTime - startTime;  // in nanoseconds
        System.out.println("Time taken: " + (duration / 1_000_000_000.0) + " sec");
        for (Point point : points){
            System.out.println(point.getCoordinates()[0]+"-"+point.getCoordinates()[1]);
        }
        System.out.println("result count: "+points.size());
    }

    public void linearRangeQuery(){
        long startTime = System.nanoTime();

        RangeQuery rq = new RangeQuery();
        ArrayList<Point> points = (ArrayList<Point>) rq.execute(Map.of(
                "lat", new double[]{41.8656791, 41.8660624},//41.8660624-12.5335863
                "lon", new double[]{12.5335863, 12.5339965}//41.8656791-12.5339965
        ), new QueryDimensions("lat", "lon"));

        long endTime = System.nanoTime();  // end timer
        long duration = endTime - startTime;  // in nanoseconds
        System.out.println("Time taken: " + (duration / 1_000_000_000.0) + " sec");
        for (Point point : points){
            System.out.println(point.getCoordinates()[0]+"-"+point.getCoordinates()[1]);
        }
        System.out.println("result count: "+points.size());
    }

    public void points(){
        long maxId = DataFileHandler.getMaxBlockId();
        for (int j=0; j<maxId; j++) {
            ArrayList<Point> points = DataFileHandler.readDataFileBlock(j + 1);
            for (Point point : points) {
                System.out.println(point.getCoordinates()[0]+"-"+point.getCoordinates()[1]);
            }
        }
    }

}
