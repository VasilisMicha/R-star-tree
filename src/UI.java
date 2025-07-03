import java.util.ArrayList;

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
}
