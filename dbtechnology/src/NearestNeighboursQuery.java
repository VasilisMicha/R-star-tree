import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class NearestNeighboursQuery extends Query {
    private final Point queryPoint;
    private final int k;

    public NearestNeighboursQuery(Point queryPoint, int k) {
        this.queryPoint = queryPoint;
        this.k = k;
    }

    @Override
    public ArrayList<Long> getQueryRecordIds(Node node) {
        PriorityQueue<IdDistancePair> heap = new PriorityQueue<>(
                (a, b) -> Double.compare(b.getDistanceFromItem(), a.getDistanceFromItem())
        );

        searchNode(node, heap);

        ArrayList<Long> results = new ArrayList<>();
        while (!heap.isEmpty()) {
            results.add(heap.poll().getRecordId());
        }
        return results;
    }

    @Override
    public ArrayList<Long> getQueryRecordIds() {
        Node root = IndexFileHandler.readNode(IndexFileHandler.getRootBlockId());
        return getQueryRecordIds(root);
    }

    private void searchNode(Node node, PriorityQueue<IdDistancePair> heap) {
        if (node.isLeaf()) {
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