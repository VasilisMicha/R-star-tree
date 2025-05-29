import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Comparator;

public class RStarTree {
    private final int maxEntries = IndexFileHandler.calculateMaxEntriesInNode();
    private final int minEntries = (int) (maxEntries * 0.4);
    private long nextBlockId = 1;
    private int currentLevel;
    private final double REINSERT_P = 0.3;
    public RStarTree() {
        IndexFileHandler.setRootBlockId(1);
        IndexFileHandler.setTreeHeight(1);
        IndexFileHandler.updateMetaDataBlock();
        createNewNode(true, -1); //creating node
    }
    public void insert(Point point) {
        Entry newEntry = new Entry(point.toRectangle(), -1, true);
        Node root = IndexFileHandler.readNode(IndexFileHandler.getRootBlockId());
        Node leaf = chooseSubtree(root, newEntry.getRectangle());
        leaf.addEntry(newEntry);
        if (leaf.getEntries().size() > maxEntries) {
            overflowTreatment(leaf, new ArrayList<>(leaf.getEntries()), getCurrentLevel(leaf));
        } else {
            IndexFileHandler.updateNode(leaf, IndexFileHandler.getTreeHeight()); // Add this
        }
        adjustTree(leaf);
    }
    private void Insert(Entry E, int level) {
        Node root = IndexFileHandler.readNode(IndexFileHandler.getRootBlockId());
        Node N = chooseSubtree(root, E.getRectangle());
        if (N.getEntries().size() < maxEntries) {
            N.addEntry(E);
        } else {
            List<Entry> allEntries = new ArrayList<>(N.getEntries());
            allEntries.add(E);
            overflowTreatment(N, allEntries, level);
        }
        adjustTree(N);
    }

    private void overflowTreatment(Node N, List<Entry> entries, int level) {
        // OT1: Reinsert or Split
//        if (level < currentLevel) {
//            reinsert(N, entries);
//        } else {
//            split(N, entries);
//        }
        split(N, entries);
    }
    private void reinsert(Node N, List<Entry> entries) {
        Rectangle nodeMBR = computeMBR(entries);
        Point center = nodeMBR.getCenter();
        entries.sort((e1, e2) ->
                Double.compare(e2.getRectangle().distanceTo(center),
                        e1.getRectangle().distanceTo(center)));
        int p = (int) (entries.size() * REINSERT_P);
        List<Entry> toReinsert = new ArrayList<>(entries.subList(0, p));
        entries = entries.subList(p, entries.size());
        N.getEntries().clear();
        N.getEntries().addAll(entries);
        updateMBR(N);
        IndexFileHandler.updateNode(N, IndexFileHandler.getTreeHeight());
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
                    cost = calculateOverlapEnlargement(entries, entry, newRect);
                } else {
                    cost = areaEnlargement;
                }
                if (cost < minCost ||
                        (cost == minCost && areaEnlargement < minAreaEnlargement) ||
                        (cost == minCost && areaEnlargement == minAreaEnlargement && area < minArea)) {
                    bestEntry = entry;
                    minCost = cost;
                    minAreaEnlargement = areaEnlargement;
                    minArea = area;
                }
            }
            current = IndexFileHandler.readNode(bestEntry.getBlockId());
        }
    }
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
    private void split(Node node, List<Entry> allEntries) {
        node.setIsPartOfTheTree(false);
        IndexFileHandler.updateNode(node, IndexFileHandler.getTreeHeight());
        int splitAxis = chooseSplitAxis(allEntries);
        List<Entry> sortedEntries = sortEntriesAlongAxis(allEntries, splitAxis, true);
        int splitIndex = chooseSplitIndex(sortedEntries, splitAxis);
        List<Entry> group1 = new ArrayList<>(sortedEntries.subList(0, splitIndex));
        List<Entry> group2 = new ArrayList<>(sortedEntries.subList(splitIndex, sortedEntries.size()));
        Node newNode1 = createNewNode(node.isLeaf(), node.getParentNodeId());
        Node newNode2 = createNewNode(node.isLeaf(), node.getParentNodeId());
        newNode1.getEntries().addAll(group1);
        newNode2.getEntries().addAll(group2);
        IndexFileHandler.updateNode(newNode1, IndexFileHandler.getTreeHeight());
        IndexFileHandler.updateNode(newNode2, IndexFileHandler.getTreeHeight());
        Node newRoot = null;
        if (node.getBlockId() == IndexFileHandler.getRootBlockId()) {
            newRoot = createNewNode(false, -1);
            newRoot.addEntry(createEntryForNode(newNode1));
            newRoot.addEntry(createEntryForNode(newNode2));
            newNode1.setParentNodeId(newRoot.getBlockId());
            newNode2.setParentNodeId(newRoot.getBlockId());
            IndexFileHandler.setRootBlockId(newRoot.getBlockId());
            IndexFileHandler.setTreeHeight(IndexFileHandler.getTreeHeight() + 1);
            IndexFileHandler.updateMetaDataBlock();
            currentLevel = (int) IndexFileHandler.getTreeHeight() - 1;
        } else {
            Node parent = IndexFileHandler.readNode(node.getParentNodeId());
            parent.getEntries().removeIf(e -> e.getBlockId() == node.getBlockId());
            parent.addEntry(createEntryForNode(newNode1));
            parent.addEntry(createEntryForNode(newNode2));
            if (parent.getEntries().size() > maxEntries) {
                overflowTreatment(parent, new ArrayList<>(parent.getEntries()), getCurrentLevel(parent));
            }
            IndexFileHandler.updateNode(parent, IndexFileHandler.getTreeHeight());
        }
        updateChildParentReferences(newNode1);
        updateChildParentReferences(newNode2);
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
    private List<Entry> sortEntriesAlongAxis(List<Entry> entries, int axis, boolean useLower) {
        List<Entry> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparingDouble(e ->
                useLower ? e.getRectangle().getMin()[axis] : e.getRectangle().getMax()[axis]));
        return sorted;
    }
    private Node createNewNode(boolean isLeaf, long parentNodeId) {
        long blockId = nextBlockId++;
        Node node = new Node(new ArrayList<>(), isLeaf, blockId, parentNodeId);
        IndexFileHandler.createNode(node); // Persist immediately
        return node;
    }
    private Entry createEntryForNode(Node node) {
        Rectangle mbr = computeMBR(node.getEntries());
        Entry entry = new Entry(mbr, node.getBlockId(), node.isLeaf());
        return entry;
    }
}