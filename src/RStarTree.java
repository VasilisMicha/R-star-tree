import java.util.*;

public class RStarTree {
    private final int maxEntries = IndexFileHandler.calculateMaxEntriesInNode();
    private final int minEntries = (int) (maxEntries * 0.4);
    private long nextBlockId = 1;
    private final double REINSERT_P = 0.3;
    private Set<Integer> reinsertedLevels = new HashSet<>();

    public RStarTree(){
        IndexFileHandler.initializeIndexFile(2,  true);
        IndexFileHandler.setRootBlockId(1);
        IndexFileHandler.setTreeHeight(1);
        IndexFileHandler.updateMetaDataBlock();
        createNewNode(true, -1); //creating node
    }


    /**
     * Inserts a new point into the R*-tree
     */
    public void insert(Point point) {
        reinsertedLevels.clear();
        Entry newEntry = new Entry(point.toRectangle(), point.getBlockId(), true);

        Node root = IndexFileHandler.readNode(IndexFileHandler.getRootBlockId());
        Node leaf = chooseSubtree(root, newEntry.getRectangle());

        leaf.addEntry(newEntry);

        if (leaf.getEntries().size() > maxEntries) {
            overflowTreatment(leaf, new ArrayList<>(leaf.getEntries()));
        } else {
            IndexFileHandler.updateNode(leaf, IndexFileHandler.getTreeHeight()); // Add this
        }
        adjustTree(leaf);
    }

    private void Insert(Entry E, int level) {
        // I1: ChooseSubtree
        Node root = IndexFileHandler.readNode(IndexFileHandler.getRootBlockId());
        Node N = chooseSubtree(root, E.getRectangle());

        // I2: Overflow check
        if (N.getEntries().size() < maxEntries) {
            N.addEntry(E);
            IndexFileHandler.updateNode(N, IndexFileHandler.getTreeHeight());
        } else {
            // I2: OverflowTreatment
            List<Entry> allEntries = new ArrayList<>(N.getEntries());
            allEntries.add(E);
            overflowTreatment(N, allEntries);
        }

        // I4: Adjust covering rectangles
        adjustTree(N);
    }

    private void overflowTreatment(Node N, List<Entry> entries) {
        // OT1: Reinsert or Split
        int level = getCurrentLevel(N);
        int rootLevel = (int) IndexFileHandler.getTreeHeight() - 1;

        if (level < rootLevel && !reinsertedLevels.contains(level)) {
            reinsertedLevels.add(level);
            reinsert(N, entries);
        } else {
            split(N, entries);
        }
//        split(N, entries);
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

        // Reinsert entries with proper level calculation
        Node root = IndexFileHandler.readNode(IndexFileHandler.getRootBlockId());
        for (Entry e : toReinsert) {
            // Find the new leaf node for this entry
            Node newLeaf = chooseSubtree(root, e.getRectangle());

            // Calculate the CORRECT level for reinsertion
            int newLevel = getCurrentLevel(newLeaf);

            // Insert with the proper level
            Insert(e, newLevel);
        }
    }

    private int getCurrentLevel(Node node) {
        int depth = 0;
        Node current = node;
        while (current != null && current.getParentNodeId() != -1) {
            depth++;
            current = IndexFileHandler.readNode(current.getParentNodeId());
        }
        return ((int) IndexFileHandler.getTreeHeight() - 1) - depth;
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
        node.setIsPartOfTheTree(false);
        IndexFileHandler.updateNode(node, IndexFileHandler.getTreeHeight());
        // S1: ChooseSplitAxis
        int splitAxis = chooseSplitAxis(allEntries);

        // S2: ChooseSplitIndex
        List<Entry> sortedEntries = sortEntriesAlongAxis(allEntries, splitAxis, true);
        int splitIndex = chooseSplitIndex(sortedEntries, splitAxis);

        // S3: Distribute entries
        List<Entry> group1 = new ArrayList<>(sortedEntries.subList(0, splitIndex));
        List<Entry> group2 = new ArrayList<>(sortedEntries.subList(splitIndex, sortedEntries.size()));

        // Create new nodes
        Node newNode1 = createNewNode(node.isLeaf(), node.getParentNodeId());
        Node newNode2 = createNewNode(node.isLeaf(), node.getParentNodeId());

        newNode1.getEntries().addAll(group1);
        newNode2.getEntries().addAll(group2);

        IndexFileHandler.updateNode(newNode1, IndexFileHandler.getTreeHeight());
        IndexFileHandler.updateNode(newNode2, IndexFileHandler.getTreeHeight());

        Node newRoot = null;
        if (node.getBlockId() == IndexFileHandler.getRootBlockId()) {
            // Create new root
            newRoot = createNewNode(false, -1);
            newRoot.addEntry(createEntryForNode(newNode1));
            newRoot.addEntry(createEntryForNode(newNode2));

            // Update parent references
            newNode1.setParentNodeId(newRoot.getBlockId());
            newNode2.setParentNodeId(newRoot.getBlockId());

            // Update tree state
            IndexFileHandler.setRootBlockId(newRoot.getBlockId());
            IndexFileHandler.setTreeHeight(IndexFileHandler.getTreeHeight() + 1);

            IndexFileHandler.updateMetaDataBlock();
        } else {
            // Update parent node
            Node parent = IndexFileHandler.readNode(node.getParentNodeId());

            // Remove old entry
            parent.getEntries().removeIf(e -> e.getBlockId() == node.getBlockId());

            // Add new entries
            parent.addEntry(createEntryForNode(newNode1));
            parent.addEntry(createEntryForNode(newNode2));

            if (parent.getEntries().size() > maxEntries) {
                overflowTreatment(parent, new ArrayList<>(parent.getEntries()));
            }
            IndexFileHandler.updateNode(parent, IndexFileHandler.getTreeHeight());
        }
        updateChildParentReferences(newNode1);
        updateChildParentReferences(newNode2);

        // Persist changes
        IndexFileHandler.updateNode(newNode1, IndexFileHandler.getTreeHeight());  // Added
        IndexFileHandler.updateNode(newNode2, IndexFileHandler.getTreeHeight());  // Added
        if (newRoot != null) {
            IndexFileHandler.updateNode(newRoot, IndexFileHandler.getTreeHeight());
        }
    }

    private void updateChildParentReferences(Node parentNode) {
        if (parentNode.isLeaf()) return;

        for (Entry entry : parentNode.getEntries()) {
            try {
                Node child = IndexFileHandler.readNode(entry.getBlockId());
                child.setParentNodeId(parentNode.getBlockId());
                IndexFileHandler.updateNode(child, IndexFileHandler.getTreeHeight());
            } catch (Exception e){
                System.out.println("Couldn't read node with block id "+entry.getBlockId()+", line 283");
                return;
            }
        }
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
     public static Rectangle computeMBR(List<Entry> entries) {
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
        IndexFileHandler.createNode(node); // Persist immediately
        return node;
    }

    /**
     * Creates an entry for a node's MBR.
     */
    private Entry createEntryForNode(Node node) {
        Rectangle mbr = computeMBR(node.getEntries());
        Entry entry = new Entry(mbr, node.getBlockId(), false);
        return entry;
    }

    /**
     * Bulk loads points into the R*-Tree using a bottom-up approach (STR algorithm)
     **/
    public void bulkLoad() {
        List<Node> leafNodes = createLeafNodes();
        int level = 0;
        while (leafNodes.size() > 1) {
            leafNodes = buildTreeLevel(leafNodes, level);
            level++;
        }
        Node root = leafNodes.get(0);
        IndexFileHandler.setRootBlockId(root.getBlockId());
        IndexFileHandler.setTreeHeight(level + 1);
        IndexFileHandler.updateMetaDataBlock();
    }

    /**
     * Creates leaf nodes from points using the Sort-Tile-Recursive (STR) algorithm
     * @return List of leaf nodes
     **/
    private List<Node> createLeafNodes() {
        List<Point> sortedPoints = sortPointsSTR();
        List<Node> leafNodes = new ArrayList<>();
        int capacity = maxEntries;
        for (int i = 0; i < sortedPoints.size(); i += capacity) {
            int end = Math.min(i + capacity, sortedPoints.size());
            List<Point> batch = sortedPoints.subList(i, end);
            List<Entry> entries = new ArrayList<>();
            for (Point point : batch) {
                entries.add(new Entry(point.toRectangle(), point.getBlockId(), true));
            }
            Node leaf = createNewNode(true, -1);
            leaf.getEntries().addAll(entries);
            IndexFileHandler.updateNode(leaf, 1);
            leafNodes.add(leaf);
        }
        return leafNodes;
    }
    /**
     * Sorts points using the Sort-Tile-Recursive algorithm
     * @return Sorted list of points
     **/
    private List<Point> sortPointsSTR() {
        ArrayList<Point> points = new ArrayList<>();
        IndexFileHandler.getDataDimensions();
        long maxId = DataFileHandler.getMaxBlockId();
        for (int j=0; j<maxId; j++) {
            ArrayList<Point> blockPoints = DataFileHandler.readDataFileBlock(j + 1);
            for (Point point : blockPoints) {
                points.add(point);
            }
        }
        List<Point> sorted = new ArrayList<>(points);
        int axis = findAxisWithLargestVariance(points);
        Collections.sort(sorted, Comparator.comparingDouble(p -> p.getCoordinates()[axis]));
        return sorted;
    }

    /**
     * Finds the axis with the largest variance in the point coordinates
     * @param points List of points
     * @return Axis index (0 for x, 1 for y, etc.)
     **/
    private int findAxisWithLargestVariance(List<Point> points) {
        int dimensions = points.get(0).getCoordinates().length;
        double[] variances = new double[dimensions];
        double[] means = new double[dimensions];
        for (Point p : points) {
            for (int i = 0; i < dimensions; i++) {
                means[i] += p.getCoordinates()[i];
            }
        }
        for (int i = 0; i < dimensions; i++) {
            means[i] /= points.size();
        }
        for (Point p : points) {
            for (int i = 0; i < dimensions; i++) {
                double diff = p.getCoordinates()[i] - means[i];
                variances[i] += diff * diff;
            }
        }
        for (int i = 0; i < dimensions; i++) {
            variances[i] /= points.size();
        }
        int maxAxis = 0;
        double maxVariance = variances[0];
        for (int i = 1; i < dimensions; i++) {
            if (variances[i] > maxVariance) {
                maxVariance = variances[i];
                maxAxis = i;
            }
        }
        return maxAxis;
    }

    /**
     * Builds a level of the tree from the nodes at the level below
     * @param nodes Nodes at the level below
     * @param level Current level (0 for leaves)
     * @return Nodes at the new level
     **/
    private List<Node> buildTreeLevel(List<Node> nodes, int level) {
        List<Node> newLevelNodes = new ArrayList<>();
        int capacity = maxEntries;
        List<Node> sortedNodes = sortNodesSTR(nodes);
        for (int i = 0; i < sortedNodes.size(); i += capacity) {
            int end = Math.min(i + capacity, sortedNodes.size());
            List<Node> batch = sortedNodes.subList(i, end);
            Node parent = createNewNode(false, -1);
            for (Node child : batch) {
                Entry entry = createEntryForNode(child);
                parent.addEntry(entry);
                child.setParentNodeId(parent.getBlockId());
                IndexFileHandler.updateNode(child, level + 2);
            }
            IndexFileHandler.updateNode(parent, level + 2);
            newLevelNodes.add(parent);
        }
        return newLevelNodes;
    }

    /**
     * Sorts nodes using the Sort-Tile-Recursive algorithm
     * @param nodes Nodes to be sorted
     * @return Sorted list of nodes
     **/
    private List<Node> sortNodesSTR(List<Node> nodes) {
        int dimensions = nodes.get(0).getEntries().get(0).getRectangle().getMin().length;
        List<Node> sorted = new ArrayList<>(nodes);
        int axis = findAxisWithLargestVarianceForNodes(nodes);
        Collections.sort(sorted, Comparator.comparingDouble(
                n -> computeMBR(n.getEntries()).getCenter().getCoordinates()[axis]));
        return sorted;
    }

    /**
     * Finds the axis with the largest variance in node centers
     * @param nodes List of nodes
     * @return Axis index (0 for x, 1 for y, etc.)
     **/
    private int findAxisWithLargestVarianceForNodes(List<Node> nodes) {
        int dimensions = nodes.get(0).getEntries().get(0).getRectangle().getMin().length;
        double[] variances = new double[dimensions];
        double[] means = new double[dimensions];
        for (Node n : nodes) {
            Point center = computeMBR(n.getEntries()).getCenter();
            for (int i = 0; i < dimensions; i++) {
                means[i] += center.getCoordinates()[i];
            }
        }
        for (int i = 0; i < dimensions; i++) {
            means[i] /= nodes.size();
        }
        for (Node n : nodes) {
            Point center = computeMBR(n.getEntries()).getCenter();
            for (int i = 0; i < dimensions; i++) {
                double diff = center.getCoordinates()[i] - means[i];
                variances[i] += diff * diff;
            }
        }
        for (int i = 0; i < dimensions; i++) {
            variances[i] /= nodes.size();
        }
        int maxAxis = 0;
        double maxVariance = variances[0];
        for (int i = 1; i < dimensions; i++) {
            if (variances[i] > maxVariance) {
                maxVariance = variances[i];
                maxAxis = i;
            }
        }
        return maxAxis;
    }

    //Deletes a point from the R-tree by creating its corresponding entry,
    //recursively removing it starting from the root and then adjusting the tree.
    public void delete(Point point) {
        Entry entryToDelete = new Entry(point.toRectangle(), point.getBlockId(), true);
        Node root = IndexFileHandler.readNode(IndexFileHandler.getRootBlockId());
        deleteRecursive(root, entryToDelete);
        adjustTree(root);
    }

    private boolean deleteRecursive(Node node, Entry entryToDelete) {
        if (node.isLeaf()) {
            //Remove entry from the leaf
            boolean removed = node.getEntries().removeIf(e ->
                    e.getBlockId() == entryToDelete.getBlockId() &&
                            Arrays.equals(e.getRectangle().getMin(), entryToDelete.getRectangle().getMin())
            );
            if (removed) {
                IndexFileHandler.updateNode(node, IndexFileHandler.getTreeHeight());
                return true;
            }
            return false;
        } else {
            //Recursive search in children
            for (Entry entry : node.getEntries()) {
                if (entry.getRectangle().contains(entryToDelete.getRectangle().getMin())) {
                    Node child = IndexFileHandler.readNode(entry.getBlockId());
                    if (deleteRecursive(child, entryToDelete)) {
                        if (child.getEntries().size() < minEntries) {
                            handleUnderflow(child);
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    }
    private void handleUnderflow(Node node) {
        //Rejoin/redistribute entries (optional for full implementation)
        System.out.println("Underflow handling not fully implemented!");
    }
}
