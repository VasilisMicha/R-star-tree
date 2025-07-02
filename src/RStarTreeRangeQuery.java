/**
 * This class implements range query execution on an R*-Tree structure.
 * It provides two ways to perform queries: either by directly specifying a rectangular region
 * or by using per-dimension filters (Map<String, double[]>). The search is performed recursively
 * starting from the root. If the node is a leaf, its entries are checked for
 * compatibility with the query region. Otherwise, the recursion continues into child nodes whose
 * bounding rectangles intersect with the search area. The implementation includes a check for invalid
 * coordinate values (NaN).
 * @author athinaanv
 **/

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RStarTreeRangeQuery {
    private final RStarTree tree;

    public RStarTreeRangeQuery(RStarTree tree) {
        this.tree = tree;
    }

    //Execute a range query using a rectangular bounding box
    public List<Point> execute(Rectangle searchRect) {
        List<Point> results = new ArrayList<>();
        rangeQueryRecursive(tree.getRoot(), searchRect, results);  //Search from the root
        return results;
    }

    //Convert dimension-based filters into a bounding rectangle
    public List<Point> execute(Map<String, double[]> rangeFilters, QueryDimensions queryDims) {
        double[] min = new double[queryDims.size()];
        double[] max = new double[queryDims.size()];
        List<String> dims = queryDims.getDimensions();

        //Set min max
        for (int i = 0; i < dims.size(); i++) {
            String dim = dims.get(i);
            if (rangeFilters.containsKey(dim)) {
                min[i] = rangeFilters.get(dim)[0];
                max[i] = rangeFilters.get(dim)[1];
            } else {
                //Else, accept all values
                min[i] = Double.NEGATIVE_INFINITY;
                max[i] = Double.POSITIVE_INFINITY;
            }
        }
        return execute(new Rectangle(min, max));
    }

    private void rangeQueryRecursive(Node node, Rectangle query, List<Point> result) {
        if (node.isLeaf()) {
            //Check the entries (points)
            for (Entry entry : node.getEntries()) {
                double[] coords = entry.getRectangle().getMin();

                if (hasNaN(coords)) continue;  //Skip if coordinates contain NaN

                if (query.contains(coords)) {
                    result.add(new Point(
                            entry.getRectangle().getId(),
                            entry.getRectangle().getName(),
                            coords
                    ));
                }
            }
        } else {
            //Check if child regions intersect the search area
            for (Entry entry : node.getEntries()) {
                if (query.intersects(entry.getRectangle())) {
                    //Read child node from storage (likely from disk)
                    Node child = IndexFileHandler.readNode(entry.getBlockId());
                    rangeQueryRecursive(child, query, result);
                }
            }
        }
    }
    //Check for NaN values
    private boolean hasNaN(double[] coords) {
        for (double d : coords) {
            if (Double.isNaN(d)) return true;
        }
        return false;
    }
}