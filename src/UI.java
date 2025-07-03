import java.util.ArrayList;
import java.util.Map;

public class UI {
    public UI(){
        DataFileHandler.initializeDataFile(2, true);
    }

    public void linearKNN(int k){
        long startTime = System.nanoTime();

        LinearSearchNearestNeighbours lknn = new LinearSearchNearestNeighbours();
        ArrayList<Point> points = (ArrayList<Point>)lknn.findKNearestNeighbors(new Point(new double[] {41.8658994, 12.53403}), 20);

        long endTime = System.nanoTime();  // end timer
        long duration = endTime - startTime;  // in nanoseconds
        System.out.println("Time taken: " + (duration / 1_000_000_000.0) + " sec");
        for (Point point : points){
            System.out.println(point.getCoordinates()[0]+"-"+point.getCoordinates()[1]);
        }
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
