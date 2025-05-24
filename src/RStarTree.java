import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Comparator;

public class RStarTree {
    private Node root;
    private final int maxEntries = IndexFileHandler.calculateMaxEntriesInNode() - 340;
    private final int minEntries = (int) (maxEntries * 0.4);
    private long nextBlockId = 1;
    private int currentLevel;
    private Set<Integer> reinsertedLevels = new HashSet<>();
    private final double REINSERT_P = 0.3;

    public RStarTree() {
        this.root = new Node(new ArrayList<>(), true, nextBlockId++);
        IndexFileHandler.setRootBlockId((int) root.getBlockId());
        IndexFileHandler.setTreeHeight(1);
        IndexFileHandler.updateNode(root, 1); // Persist root node
    }


    /**
     * Inserts a new point into the R*-tree
     */
     public void insert(Point point) {
        Entry newEntry = new Entry(point.toRectangle(), -1, true);
        root = IndexFileHandler.readNode(IndexFileHandler.getRootBlockId());
        Node leaf = chooseSubtree(root, newEntry.getRectangle());
        leaf.addEntry(newEntry);

        if (leaf.getEntries().size() > maxEntries) {
            overflowTreatment(leaf, new ArrayList<>(leaf.getEntries()), getCurrentLevel(leaf));
        } else {
            IndexFileHandler.updateNode(leaf, IndexFileHandler.getTreeHeight()); // Add this
        }
        root = IndexFileHandler.readNode(IndexFileHandler.getRootBlockId());
        adjustTree(leaf);
    }

    private void Insert(Entry E, int level) {
        // I1: ChooseSubtree
        Node N = chooseSubtree(root, E.getRectangle());

        // I2: Overflow check
        if (N.getEntries().size() < maxEntries) {
            N.addEntry(E);
        } else {
            // I2: OverflowTreatment
            List<Entry> allEntries = new ArrayList<>(N.getEntries());
            allEntries.add(E);
            overflowTreatment(N, allEntries, level);
        }

        // I4: Adjust covering rectangles
        adjustTree(N);
    }

    private void overflowTreatment(Node N, List<Entry> entries, int level) {
        // OT1: Reinsert or Split
        if (level < currentLevel) {
            System.out.println("reinsert");
            reinsert(N, entries);
        } else {
            System.out.println("split");
            split(N, entries);
        }
    }

    private void reinsert(Node N, List<Entry> entries) {
        // RI1: Compute distances
        Rectangle nodeMBR = computeMBR(entries);
        Point center = nodeMBR.getCenter();

        entries.sort((e1, e2) ->
                Double.compare(e2.getRectangle().distanceTo(center),
                        e1.getRectangle().distanceTo(center)));

        // RI2: Remove first p entries
        int p = (int) (entries.size() * REINSERT_P);
        List<Entry> toReinsert = new ArrayList<>(entries.subList(0, p));
        entries = entries.subList(p, entries.size());

        // Update node
        N.getEntries().clear();
        N.getEntries().addAll(entries);
        updateMBR(N);
        IndexFileHandler.updateNode(N, IndexFileHandler.getTreeHeight());

        // Reinsert
        toReinsert.forEach(e -> Insert(e, currentLevel));
    }

    private int getCurrentLevel(Node node) {
        int level = 0;
        Node current = node;
        while (current != null && current.getParentNodeId() != -1) {
            level++;
            current = IndexFileHandler.readNode(current.getParentNodeId());
        }
        return ++level;
    }

    private void adjustTree(Node node) {
        Set<Long> visited = new HashSet<>(); // Prevent cycles
        while (node != null && !visited.contains(node.getBlockId())) {
            visited.add(node.getBlockId());
            if (updateMBR(node)) {  // Modified to return boolean
                IndexFileHandler.updateNode(node, IndexFileHandler.getTreeHeight());
            }
            node = node.getParentNodeId() != -1
                    ? IndexFileHandler.readNode(node.getParentNodeId())
                    : null;
        }
    }

    private boolean updateMBR(Node node) {
        boolean changed = false;
        if (!node.isLeaf()) {
            for (Entry entry : node.getEntries()) {
                Node child = IndexFileHandler.readNode(entry.getBlockId());
                Rectangle newMBR = computeMBR(child.getEntries());
                if (!newMBR.equals(entry.getRectangle())) {
                    entry.setRectangle(newMBR);
                    changed = true;
                }
            }
        }
        return changed;
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
     * Splits an overflowing node according to R*-tree rules
     */
    private void split(Node node, List<Entry> allEntries) {
        // S1: ChooseSplitAxis
        int splitAxis = chooseSplitAxis(allEntries);

        // S2: ChooseSplitIndex
        List<Entry> sortedEntries = sortEntriesAlongAxis(allEntries, splitAxis, true);
        int splitIndex = chooseSplitIndex(sortedEntries, splitAxis);

        // S3: Distribute entries
        List<Entry> group1 = new ArrayList<>(sortedEntries.subList(0, splitIndex));
        List<Entry> group2 = new ArrayList<>(sortedEntries.subList(splitIndex, sortedEntries.size()));

        // Create new nodes
        Node newNode1 = createNewNode(node.isLeaf(), node.getBlockId());
        Node newNode2 = createNewNode(node.isLeaf(), node.getBlockId());

        newNode1.getEntries().addAll(group1);
        newNode2.getEntries().addAll(group2);

        System.out.println("node that is getting split: "+node.getBlockId());
        if (node.getBlockId() == IndexFileHandler.getRootBlockId()) {
            System.out.println("root");
            // Create new root
            Node newRoot = createNewNode(false, -1);
            newRoot.addEntry(createEntryForNode(newNode1));
            newRoot.addEntry(createEntryForNode(newNode2));

            // Update parent references
            newNode1.setParentNodeId(newRoot.getBlockId());
            newNode2.setParentNodeId(newRoot.getBlockId());

            // Update tree state
            root = newRoot;
            IndexFileHandler.setRootBlockId(newRoot.getBlockId());
            IndexFileHandler.setTreeHeight(IndexFileHandler.getTreeHeight() + 1);

            IndexFileHandler.updateMetaDataBlock();

            node.getEntries().clear();
            IndexFileHandler.updateNode(node, IndexFileHandler.getTreeHeight());
            IndexFileHandler.updateNode(newRoot, IndexFileHandler.getTreeHeight());

            currentLevel = (int) IndexFileHandler.getTreeHeight() - 1;
        } else {
            System.out.println("not root");
            // Update parent node
            Node parent = IndexFileHandler.readNode(node.getParentNodeId());

            // Remove old entry
            parent.getEntries().removeIf(e -> e.getBlockId() == node.getBlockId());

            // Add new entries
            parent.addEntry(createEntryForNode(newNode1));
            parent.addEntry(createEntryForNode(newNode2));

            if (parent.getEntries().size() > maxEntries){
                overflowTreatment(parent, new ArrayList<>(parent.getEntries()), getCurrentLevel(parent));
            } else {
                newNode1.setParentNodeId(parent.getBlockId());
                newNode2.setParentNodeId(parent.getBlockId());
            }

            node.getEntries().clear();
            IndexFileHandler.updateNode(node, IndexFileHandler.getTreeHeight());
            IndexFileHandler.updateNode(parent, IndexFileHandler.getTreeHeight());
        }

        // Persist changes
        IndexFileHandler.updateNode(newNode1, IndexFileHandler.getTreeHeight());
        IndexFileHandler.updateNode(newNode2, IndexFileHandler.getTreeHeight());
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
        IndexFileHandler.updateNode(node, IndexFileHandler.getTreeHeight()); // Persist immediately
        return node;
    }

    /**
     * Creates an entry for a node's MBR.
     */
    private Entry createEntryForNode(Node node) {
        Rectangle mbr = computeMBR(node.getEntries());
        Entry entry = new Entry(mbr, node.getBlockId(), node.isLeaf());
        IndexFileHandler.updateNode(node, IndexFileHandler.getTreeHeight()); // Update node after creating entry
        return entry;
    }
}
