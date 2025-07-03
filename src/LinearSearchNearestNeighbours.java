import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class LinearSearchNearestNeighbours {

    public LinearSearchNearestNeighbours() {}


    public List<Point> findKNearestNeighbors(Point queryPoint, int k) {
        PriorityQueue<PointDistancePair> heap = new PriorityQueue<>(
                Comparator.comparingDouble(PointDistancePair::getDistance).reversed()
        );

        long maxId = DataFileHandler.getMaxBlockId();
        for (int i=0; i<maxId; i++) {
            ArrayList<Point> points = DataFileHandler.readDataFileBlock(i+1);
            for (Point point : points) {
                double distance = point.distanceTo(queryPoint);
                if (heap.size() < k) {
                    heap.add(new PointDistancePair(point, distance));
                } else if (distance < heap.peek().getDistance()) {
                    heap.poll();
                    heap.add(new PointDistancePair(point, distance));
                }
            }
        }

        List<Point> result = new ArrayList<>();
        while (!heap.isEmpty()) {
            result.add(heap.poll().getPoint());
        }
        result.sort(Comparator.comparingDouble(p -> p.distanceTo(queryPoint)));
        return result;
    }

    private static class PointDistancePair {
        private final Point point;
        private final double distance;

        public PointDistancePair(Point point, double distance) {
            this.point = point;
            this.distance = distance;
        }

        public Point getPoint() {
            return point;
        }

        public double getDistance() {
            return distance;
        }
    }
}