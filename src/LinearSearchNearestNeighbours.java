/**
 * Implements a simple linear search algorithm to find the k nearest neighbors
 * of a query point from a list of points. This approach is straightforward.
 **/

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class LinearSearchNearestNeighbours {
    //List of points to search from
    private final List<Point> points;

    //Constructor that initializes the point list
    public LinearSearchNearestNeighbours(List<Point> points) {
        this.points = points;
    }

    //Find the k nearest neighbors to the queryPoint using linear search
    public List<Point> findKNearestNeighbors(Point queryPoint, int k) {
        //Max-heap (priority queue) based on distance
        PriorityQueue<PointDistancePair> heap = new PriorityQueue<>(
                Comparator.comparingDouble(PointDistancePair::getDistance).reversed()
        );

        //Iterate over all points
        for (Point point : points) {
            double distance = point.distanceTo(queryPoint); //Calculate distance to queryPoint

            //If heap not full, add the current point
            if (heap.size() < k) {
                heap.add(new PointDistancePair(point, distance));
            }
            //Otherwise, if the current point is closer than the farthest in heap, replace it
            else if (distance < heap.peek().getDistance()) {
                heap.poll();
                heap.add(new PointDistancePair(point, distance)); //Add closer point
            }
        }

        //Extract points from heap into a list
        List<Point> result = new ArrayList<>();
        while (!heap.isEmpty()) {
            result.add(heap.poll().getPoint());
        }

        //Sort final results by distance ascending
        result.sort(Comparator.comparingDouble(p -> p.distanceTo(queryPoint)));
        return result;
    }

    //Helper class to hold point and its distance to the query point
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
