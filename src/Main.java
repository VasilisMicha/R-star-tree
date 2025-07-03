import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        DataFileHandler.initializeDataFile(2, true);
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
}
