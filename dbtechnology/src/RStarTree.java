/**
 * Implementation of the R*-Tree data structure for efficient indexing of multidimensional spatial data.
 * Supports dynamic insertion and range queries using MBRs.
 * Uses area enlargement as the primary heuristic for subtree selection and supports node splitting.
 */


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RStarTree {
    private Node root;
    private final int dimensions;
    static final int MAX_ENTRIES = 4;   // Maximum number of entries per node before splitting


    public RStarTree(int dimensions) {
        this.dimensions = dimensions;
        this.root = new LeafNode(dimensions);
    }

    //Inserts a new point into the R*-Tree.
    public void insert(Point point) {
        if (point == null) return;

        double[] coords = point.getCoordinates();
        if (coords.length != dimensions) return;

        for (double val : coords) {
            if (Double.isNaN(val)) {
                return;  // Silently skip points with invalid (NaN) coordinates
            }
        }

        Node newChild = insertRecursive(root, point);
        if (newChild != null) {
            InternalNode newRoot = new InternalNode(root.getLevel() + 1);
            newRoot.addEntry(new Entry(root.getMBR(), root));
            newRoot.addEntry(new Entry(newChild.getMBR(), newChild));
            root = newRoot;
        }
    }

    //Recursively inserts a point into the tree and handles node splitting (if necessary).
    private Node insertRecursive(Node node, Point point) {
        if (node.isLeaf()) {
            LeafNode leaf = (LeafNode) node;
            return leaf.addEntry(new LeafEntry(point, 0, 0));
        } else {
            InternalNode internal = (InternalNode) node;

            //Check if there are entries
            if (internal.getEntries().isEmpty()) {
                //If none exist, create a new LeafNode
                LeafNode newLeaf = new LeafNode(dimensions);
                Node sibling = newLeaf.addEntry(new LeafEntry(point, 0, 0));
                internal.addEntry(new Entry(newLeaf.getMBR(), newLeaf));
                return sibling;
            }

            Entry best = findBestEntry(internal, point);
            //Null check
            if (best == null) {
                throw new IllegalStateException("No suitable entry found for point: " + point);
            }

            Node sibling = insertRecursive(best.getChild(), point);
            if (sibling != null) {
                internal.addEntry(new Entry(sibling.getMBR(), sibling));
                if (internal.getEntries().size() > MAX_ENTRIES) {
                    return splitInternalNode(internal);
                }
            }
            return null;
        }
    }

    //Finds the best entry in an internal node to insert a given point based on min area enlargement.
    private Entry findBestEntry(InternalNode node, Point point) {
        if (node.getEntries().isEmpty()) {
            throw new IllegalStateException("InternalNode has no entries");
        }

        Entry best = null;
        double minEnlargement = Double.MAX_VALUE;

        for (Entry entry : node.getEntries()) {
            if (entry == null || entry.getRectangle() == null) {
                continue;
            }

            double enlargement = computeEnlargement(entry.getRectangle(), point);
            if (enlargement < minEnlargement) {
                minEnlargement = enlargement;
                best = entry;
            }
        }

        if (best == null) {
            best = node.getEntries().get(0);  //Fallback
        }
        return best;
    }

    //Splits an internal node into two nodes when it exceeds MAX_ENTRIES.
    private Node splitInternalNode(InternalNode node) {
        List<Entry> entries = new ArrayList<>(node.getEntries());
        node.getEntries().clear();

        int mid = entries.size()/2;
        InternalNode sibling = new InternalNode(node.getLevel());

        for (int i = 0; i < entries.size(); i++) {
            if (i < mid) {
                node.addEntry(entries.get(i));
            } else {
                sibling.addEntry(entries.get(i));
            }
        }

        return sibling;
    }

    //Computes the area enlargement required for a rectangle to include a new point.
    private double computeEnlargement(Rectangle rect, Point point) {
        double[] min = rect.getMin().clone();
        double[] max = rect.getMax().clone();
        double[] coords = point.getCoordinates();

        for (int i = 0; i < coords.length; i++) {
            min[i] = Math.min(min[i], coords[i]);
            max[i] = Math.max(max[i], coords[i]);
        }

        return calculateArea(min, max) - calculateArea(rect.getMin(), rect.getMax());
    }

    private double calculateArea(double[] min, double[] max) {
        double area = 1.0;
        for (int i = 0; i < min.length; i++) {
            area *= (max[i] - min[i]);
        }
        return area;
    }

//******************//*******************//********************//******************//

    //Executes a range query using a predefined Rectangle class
    public List<Point> rangeQuery(Rectangle searchRect) {
        List<Point> result = new ArrayList<>();
        rangeQueryRecursive(root, searchRect, result);
        return result;
    }


    //Executes a range query using dimension-based filters
    public List<Point> rangeQuery(Map<String, double[]> rangeFilters, QueryDimensions queryDims) {

        double[] min = new double[queryDims.size()];
        double[] max = new double[queryDims.size()];

        List<String> dimensions = queryDims.getDimensions();
        for (int i = 0; i < dimensions.size(); i++) {
            String dim = dimensions.get(i);
            if (rangeFilters.containsKey(dim)) {
                min[i] = rangeFilters.get(dim)[0];
                max[i] = rangeFilters.get(dim)[1];
            } else {
                //If no range was specified, use extreme values
                min[i] = Double.NEGATIVE_INFINITY;
                max[i] = Double.POSITIVE_INFINITY;
            }
        }

        Rectangle searchRect = new Rectangle(min, max);
        return rangeQuery(searchRect);
    }

     //Recursively traverses the tree to find points within the query rectangle.
    private void rangeQueryRecursive(Node node, Rectangle query, List<Point> result) {
        if (node.isLeaf()) {
            for (LeafEntry entry : ((LeafNode) node).getEntries()) {
                if (query.contains(entry.getPoint())) {
                    result.add(entry.getPoint());
                }
            }
        } else {
            for (Entry entry : ((InternalNode) node).getEntries()) {
                if (query.intersects(entry.getRectangle())) {
                    rangeQueryRecursive(entry.getChild(), query, result);
                }
            }
        }
    }
}
