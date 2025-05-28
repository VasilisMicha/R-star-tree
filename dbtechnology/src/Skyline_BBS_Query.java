/**
 * Computes the skyline points from a multidimensional dataset indexed by an R*-Tree.
 * The skyline contains points not dominated by others (no worse in all dimensions and better in at least one).
 * It uses a priority queue for best-first traversal, pruning subtrees dominated by the skyline.
 * Filters out points with NaN coordinates to ensure valid results.
 * @author athinaanv
 **/

import java.util.*;

public class Skyline_BBS_Query {
    public static List<Point> computeSkyline(RStarTree tree) {
        List<Point> skyline = new ArrayList<>();
        //Priority queue sorted by the sum of coords
        //Lower sum = higher priority
        PriorityQueue<NodeEntry> queue = new PriorityQueue<>(Comparator.comparingDouble(e -> e.priority));

        queue.add(new NodeEntry(tree.getRoot(), 0.0));  //Start from the root

        while (!queue.isEmpty()) {
            NodeEntry entry = queue.poll();
            Node node = entry.node;

            if (node.isLeaf()) {
                //Leaf node: entries are data points
                for (Entry e : node.getEntries()) {
                    Point p = entryToPoint(e);
                    if (containsNaN(p)) continue;  //Skip invalid data

                    //If is not dominated, add it and remove dominated ones
                    if (!isDominated(p, skyline)) {
                        skyline.removeIf(s -> dominates(p, s));
                        skyline.add(p);
                    }
                }
            } else {
                //Internal node: expand entries that are not dominated
                for (Entry childEntry : node.getEntries()) {
                    if (childEntry == null || childEntry.getRectangle() == null) continue;
                    double priority = sumCoordinates(childEntry.getRectangle().getMin());
                    if (!isDominated(childEntry.getRectangle(), skyline)) {
                        Node childNode = IndexFileHandler.readNode(childEntry.getBlockId());
                        queue.add(new NodeEntry(childNode, priority));
                    }
                }
            }
        }

        return skyline;
    }

    //Convert a rectangle to a point assuming it's a degenerate rectangle
    //min == max
    private static Point entryToPoint(Entry e) {
        double[] coords = e.getRectangle().getMin();
        return new Point(coords);
    }

    //Check if a point is dominated by any point in the current skyline
    private static boolean isDominated(Point p, List<Point> skyline) {
        for (Point s : skyline) {
            if (dominates(s, p)) return true;
        }
        return false;
    }

    //Check if a rectangle is dominated by any point in the skyline
    private static boolean isDominated(Rectangle rect, List<Point> skyline) {
        for (Point s : skyline) {
            if (dominates(s.getCoordinates(), rect.getMin())) return true;
        }
        return false;
    }

    private static boolean dominates(Point a, Point b) {
        return dominates(a.getCoordinates(), b.getCoordinates());
    }

    //"a" dominates "b" if it is no worse in all dimensions and better in at least one
    private static boolean dominates(double[] a, double[] b) {
        boolean better = false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > b[i]) return false;  //The worst in one dimension -> not dominating
            if (a[i] < b[i]) better = true;
        }
        return better;
    }

    //Check for NaN coordinates
    private static boolean containsNaN(Point p) {
        for (double d : p.getCoordinates()) {
            if (Double.isNaN(d)) return true;
        }
        return false;
    }

    //Calculate the sum of coordinates (priority metric)
    private static double sumCoordinates(double[] coords) {
        double sum = 0.0;
        for (double c : coords) sum += c;
        return sum;
    }

    //Helper class
    private static class NodeEntry {
        Node node;
        double priority;

        NodeEntry(Node node, double priority) {
            this.node = node;
            this.priority = priority;
        }
    }
}
