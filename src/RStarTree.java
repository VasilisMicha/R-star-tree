import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class RStarTree {
    private Node root;
    private final int maxEntries;
    private final int minEntries;
    private long nextBlockId = 1;

    public RStarTree(int maxEntries) {
        this.maxEntries = maxEntries;
        this.minEntries = (int) (maxEntries * 0.4); // Common heuristic
        this.root = new Node(new ArrayList<>(), true, nextBlockId++);
    }

    /**
     * Inserts a new point into the R*-tree
     */
    public void insert(Point point) {
        Rectangle rect = point.toRectangle();
        Node leaf = chooseSubtree(root, rect);
        // Rest of insertion logic would go here
    }

    /**
     * Implementation of the ChooseSubtree algorithm
     */
    private Node chooseSubtree(Node node, Rectangle newRect) {
        Node current = node;

        while (true) {
            if (current.isLeaf()) {
                return current;
            }

            List<Entry> entries = current.getEntries();
            Entry bestEntry = null;
            double minCost = Double.POSITIVE_INFINITY;
            double minAreaEnlargement = Double.POSITIVE_INFINITY;
            double minArea = Double.POSITIVE_INFINITY;

            boolean childrenAreLeaves = entries.get(0).isLeaf();

            for (Entry entry : entries) {
                Rectangle entryRect = entry.getRectangle();

                double cost;
                double areaEnlargement = entryRect.calculateAreaEnlargement(newRect);
                double area = entryRect.calculateArea();

                if (childrenAreLeaves) {
                    // Calculate overlap cost
                    cost = calculateOverlapEnlargement(entries, entry, newRect);
                } else {
                    // Use area enlargement
                    cost = areaEnlargement;
                }

                // Apply tie-breaking rules
                if (cost < minCost ||
                        (cost == minCost && areaEnlargement < minAreaEnlargement) ||
                        (cost == minCost && areaEnlargement == minAreaEnlargement && area < minArea)) {

                    bestEntry = entry;
                    minCost = cost;
                    minAreaEnlargement = areaEnlargement;
                    minArea = area;
                }
            }

            // Move to the next level
            current = IndexFileHandler.readNode(bestEntry.getBlockId());
        }
    }

    /**
     * Calculates the overlap enlargement for an entry if it were enlarged to include newRect
     */
    private double calculateOverlapEnlargement(List<Entry> entries, Entry entry, Rectangle newRect) {
        Rectangle enlarged = entry.getRectangle().enlarge(newRect);
        double totalOverlapEnlargement = 0.0;

        for (Entry other : entries) {
            if (other == entry) continue;

            Rectangle otherRect = other.getRectangle();
            double originalOverlap = entry.getRectangle().calculateOverlap(otherRect);
            double newOverlap = enlarged.calculateOverlap(otherRect);
            totalOverlapEnlargement += (newOverlap - originalOverlap);
        }

        return totalOverlapEnlargement;
    }

    /**
     * Splits an overflowing node into two new nodes and updates the tree structure.
     */
    private void splitNode(Node node) {
        List<Entry> entries = node.getEntries();
        int splitAxis = chooseSplitAxis(entries);
        List<Entry> sortedEntries = sortEntriesAlongAxis(entries, splitAxis, true);
        int splitIndex = chooseSplitIndex(sortedEntries, splitAxis);

        // Create two groups
        List<Entry> group1 = new ArrayList<>(sortedEntries.subList(0, splitIndex));
        List<Entry> group2 = new ArrayList<>(sortedEntries.subList(splitIndex, sortedEntries.size()));

        // Create new nodes with parent reference
        Node newNode1 = createNewNode(node.isLeaf(), node.getParentNodeId());
        Node newNode2 = createNewNode(node.isLeaf(), node.getParentNodeId());
        newNode1.getEntries().addAll(group1);
        newNode2.getEntries().addAll(group2);

        if (node == root) {
            // Handle root split
            Node newRoot = createNewNode(false, -1); // New root has no parent
            newRoot.addEntry(createEntryForNode(newNode1));
            newRoot.addEntry(createEntryForNode(newNode2));

            // Update children's parent references
            newNode1.setParentNodeId(newRoot.getBlockId());
            newNode2.setParentNodeId(newRoot.getBlockId());

            root = newRoot;
        } else {
            // Get parent node
            Node parent = IndexFileHandler.readNode(node.getParentNodeId());

            // Remove old entry pointing to original node
            parent.getEntries().removeIf(e -> e.getBlockId() == node.getBlockId());

            // Add new entries
            parent.addEntry(createEntryForNode(newNode1));
            parent.addEntry(createEntryForNode(newNode2));

            // Update parent references
            newNode1.setParentNodeId(parent.getBlockId());
            newNode2.setParentNodeId(parent.getBlockId());

            // Check if parent needs splitting
            if (parent.getEntries().size() > maxEntries) {
                splitNode(parent);
            }
        }

        // Update storage (would call IndexFileHandler in real implementation)
//        updateNodeInStorage(parent);
//        updateNodeInStorage(newNode1);
//        updateNodeInStorage(newNode2);
    }

    /**
     * Chooses the split axis using the minimum margin sum (S) criterion.
     */
    private int chooseSplitAxis(List<Entry> entries) {
        int dimensions = entries.get(0).getRectangle().getMin().length;
        double minS = Double.POSITIVE_INFINITY;
        int bestAxis = 0;

        for (int axis = 0; axis < dimensions; axis++) {
            double S = computeMarginSum(entries, axis, true) +
                    computeMarginSum(entries, axis, false);
            if (S < minS) {
                minS = S;
                bestAxis = axis;
            }
        }
        return bestAxis;
    }

    /**
     * Computes the margin sum (S) for a sorted list of entries along an axis.
     */
    private double computeMarginSum(List<Entry> entries, int axis, boolean useLower) {
        List<Entry> sorted = sortEntriesAlongAxis(entries, axis, useLower);
        double sum = 0.0;
        int size = sorted.size();

        for (int k = minEntries - 1; k < size - minEntries; k++) {
            int splitIndex = k + 1;
            List<Entry> group1 = sorted.subList(0, splitIndex);
            List<Entry> group2 = sorted.subList(splitIndex, size);

            Rectangle mbr1 = computeMBR(group1);
            Rectangle mbr2 = computeMBR(group2);
            sum += mbr1.calculateMargin() + mbr2.calculateMargin();
        }
        return sum;
    }

    /**
     * Chooses the split index along the chosen axis using minimal overlap.
     */
    private int chooseSplitIndex(List<Entry> sortedEntries, int axis) {
        int bestSplitIndex = minEntries;
        double minOverlap = Double.POSITIVE_INFINITY;
        double minArea = Double.POSITIVE_INFINITY;
        int size = sortedEntries.size();

        for (int k = minEntries - 1; k < size - minEntries; k++) {
            int splitIndex = k + 1;
            List<Entry> group1 = sortedEntries.subList(0, splitIndex);
            List<Entry> group2 = sortedEntries.subList(splitIndex, size);

            Rectangle mbr1 = computeMBR(group1);
            Rectangle mbr2 = computeMBR(group2);

            double overlap = mbr1.calculateOverlap(mbr2);
            double area = mbr1.calculateArea() + mbr2.calculateArea();

            if (overlap < minOverlap || (overlap == minOverlap && area < minArea)) {
                minOverlap = overlap;
                minArea = area;
                bestSplitIndex = splitIndex;
            }
        }
        return bestSplitIndex;
    }

    /**
     * Computes the MBR of a list of entries.
     */
    private Rectangle computeMBR(List<Entry> entries) {
        if (entries.isEmpty()) throw new IllegalArgumentException("Empty entries");
        double[] min = entries.get(0).getRectangle().getMin().clone();
        double[] max = entries.get(0).getRectangle().getMax().clone();
        int dimensions = min.length;

        for (Entry entry : entries) {
            Rectangle rect = entry.getRectangle();
            for (int i = 0; i < dimensions; i++) {
                min[i] = Math.min(min[i], rect.getMin()[i]);
                max[i] = Math.max(max[i], rect.getMax()[i]);
            }
        }
        return new Rectangle(min, max);
    }

    /**
     * Sorts entries along a specified axis by lower or upper value.
     */
    private List<Entry> sortEntriesAlongAxis(List<Entry> entries, int axis, boolean useLower) {
        List<Entry> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparingDouble(e ->
                useLower ? e.getRectangle().getMin()[axis] : e.getRectangle().getMax()[axis]));
        return sorted;
    }

    /**
     * Creates a new node and stores it in the index file.
     */
    private Node createNewNode(boolean isLeaf, long parentNodeId) {
        long blockId = nextBlockId++;
        Node node = new Node(new ArrayList<>(), isLeaf, blockId, parentNodeId);
        // Placeholder: Save node to index file using IndexFileHandler
        return node;
    }

    /**
     * Creates an entry for a node's MBR.
     */
    private Entry createEntryForNode(Node node) {
        Rectangle mbr = computeMBR(node.getEntries());
        return new Entry(mbr, node.getBlockId(), 1, node.isLeaf());
    }
}
