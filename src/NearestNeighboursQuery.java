
/**
 * This class implements a k-nearest neighbor search query on a tree-based spatial index.
 * The query traverses nodes starting from the root, pruning subtrees using minimum distance 
 * estimates, and collects the k closest record IDs to the query point.
 * It uses a max-heap to maintain the closest results found so far.
 **/

import java.util.ArrayList;
import java.util.PriorityQueue;

public class NearestNeighboursQuery extends Query {
  // The query point to find neighbors around
  private final Point queryPoint;
  private final int k;

  // Constructor initializes the query point and number of neighbors
  public NearestNeighboursQuery(Point queryPoint, int k) {
    this.queryPoint = queryPoint;
    this.k = k;
  }

  @Override
  public ArrayList<Long> getQueryRecordIds(Node node) {
    // Max-heap of IdDistancePair
    PriorityQueue<IdDistancePair> heap = new PriorityQueue<>(
        (a, b) -> Double.compare(b.getDistanceFromItem(), a.getDistanceFromItem()));

    // Recursively search the node and update heap with the closest entries
    searchNode(node, heap);

    // Extract record IDs from the heap into a list
    ArrayList<Long> results = new ArrayList<>();
    while (!heap.isEmpty()) {
      results.add(heap.poll().getRecordId());
    }
    return results;
  }

  @Override
  public ArrayList<Long> getQueryRecordIds() {
    // Start from the root node of the index file
    Node root = IndexFileHandler.readNode(IndexFileHandler.getRootBlockId());
    return getQueryRecordIds(root);
  }

  private void searchNode(Node node, PriorityQueue<IdDistancePair> heap) {
    if (node.isLeaf()) {
      // At leaf node, calculate distance to each entry and update
      for (Entry entry : node.getEntries()) {
        double distance = queryPoint.distanceTo(new Point(entry.getRectangle().getMin()));
        if (heap.size() < k) {
          heap.add(new IdDistancePair(entry.getBlockId(), distance));
        } else if (distance < heap.peek().getDistanceFromItem()) {
          heap.poll();
          heap.add(new IdDistancePair(entry.getBlockId(), distance));
        }
      }
    } else {
      // For internal nodes, traverse child nodes whose minimum distance could improve
      // results
      for (Entry entry : node.getEntries()) {
        double minDist = entry.getRectangle().minDistanceTo(queryPoint);
        if (heap.size() < k || minDist < heap.peek().getDistanceFromItem()) {
          Node child = IndexFileHandler.readNode(entry.getBlockId());
          searchNode(child, heap);
        }
      }
    }
  }
}
