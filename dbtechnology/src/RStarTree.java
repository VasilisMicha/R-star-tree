//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RStarTree {
    private final int maxEntries = DataFileHandler.calculateMaxEntriesInNode() - 360;
    private final int minEntries;
    private long nextBlockId;
    private int currentLevel;
    private Set<Integer> reinsertedLevels;
    private final double REINSERT_P;

    public RStarTree() {
        this.minEntries = (int)((double)this.maxEntries * 0.4);
        this.nextBlockId = 1L;
        this.reinsertedLevels = new HashSet();
        this.REINSERT_P = 0.3;
        IndexFileHandler.setRootBlockId(1L);
        IndexFileHandler.setTreeHeight(1L);
        IndexFileHandler.updateMetaDataBlock();
        this.createNewNode(true, -1L);
    }

    public void insert(Point point) {
        Entry newEntry = new Entry(point.toRectangle(), -1L, true);
        Node root = IndexFileHandler.readNode(IndexFileHandler.getRootBlockId());
        Node leaf = this.chooseSubtree(root, newEntry.getRectangle());
        leaf.addEntry(newEntry);
        if (leaf.getEntries().size() > this.maxEntries) {
            this.overflowTreatment(leaf, new ArrayList(leaf.getEntries()), this.getCurrentLevel(leaf));
        } else {
            IndexFileHandler.updateNode(leaf, IndexFileHandler.getTreeHeight());
        }

        this.adjustTree(leaf);
    }

    private void Insert(Entry E, int level) {
        Node root = IndexFileHandler.readNode(IndexFileHandler.getRootBlockId());
        Node N = this.chooseSubtree(root, E.getRectangle());
        if (N.getEntries().size() < this.maxEntries) {
            N.addEntry(E);
        } else {
            List<Entry> allEntries = new ArrayList(N.getEntries());
            allEntries.add(E);
            this.overflowTreatment(N, allEntries, level);
        }

        this.adjustTree(N);
    }

    private void overflowTreatment(Node N, List<Entry> entries, int level) {
        System.out.println("oveflow");
        if (level < this.currentLevel) {
            this.reinsert(N, entries);
        } else {
            this.split(N, entries);
        }

    }

    private void reinsert(Node N, List<Entry> entries) {
        Rectangle nodeMBR = this.computeMBR(entries);
        Point center = nodeMBR.getCenter();
        entries.sort((e1, e2) -> {
            return Double.compare(e2.getRectangle().distanceTo(center), e1.getRectangle().distanceTo(center));
        });
        int p = (int)((double)entries.size() * 0.3);
        List<Entry> toReinsert = new ArrayList(entries.subList(0, p));
        entries = entries.subList(p, entries.size());
        N.getEntries().clear();
        N.getEntries().addAll(entries);
        this.updateMBR(N);
        IndexFileHandler.updateNode(N, IndexFileHandler.getTreeHeight());
        toReinsert.forEach((e) -> {
            this.Insert(e, this.currentLevel);
        });
    }

    private int getCurrentLevel(Node node) {
        int level = 0;

        for(Node current = node; current != null && current.getParentNodeId() != -1L; current = IndexFileHandler.readNode(current.getParentNodeId())) {
            ++level;
        }

        ++level;
        return level;
    }

    private void adjustTree(Node node) {
        for(Set<Long> visited = new HashSet(); node != null && !visited.contains(node.getBlockId()); node = node.getParentNodeId() != -1L ? IndexFileHandler.readNode(node.getParentNodeId()) : null) {
            visited.add(node.getBlockId());
            if (this.updateMBR(node)) {
                IndexFileHandler.updateNode(node, IndexFileHandler.getTreeHeight());
            }
        }

    }

    private boolean updateMBR(Node node) {
        boolean changed = false;
        if (!node.isLeaf()) {
            Iterator var3 = node.getEntries().iterator();

            while(var3.hasNext()) {
                Entry entry = (Entry)var3.next();
                Node child = IndexFileHandler.readNode(entry.getBlockId());
                PrintStream var10000 = System.out;
                long var10001 = child.getBlockId();
                var10000.println("" + var10001 + ": " + child.getEntries().size());
                Rectangle newMBR = this.computeMBR(child.getEntries());
                if (!newMBR.equals(entry.getRectangle())) {
                    entry.setRectangle(newMBR);
                    changed = true;
                }
            }
        }

        return changed;
    }

    private Node chooseSubtree(Node node, Rectangle newRect) {
        Node current;
        Entry bestEntry;
        label44:
        for(current = node; !current.isLeaf(); current = IndexFileHandler.readNode(bestEntry.getBlockId())) {
            List<Entry> entries = current.getEntries();
            bestEntry = null;
            double minCost = Double.POSITIVE_INFINITY;
            double minAreaEnlargement = Double.POSITIVE_INFINITY;
            double minArea = Double.POSITIVE_INFINITY;
            boolean childrenAreLeaves = ((Entry)entries.get(0)).isLeaf();
            Iterator var13 = entries.iterator();

            while(true) {
                Entry entry;
                double cost;
                double areaEnlargement;
                double area;
                do {
                    if (!var13.hasNext()) {
                        continue label44;
                    }

                    entry = (Entry)var13.next();
                    Rectangle entryRect = entry.getRectangle();
                    areaEnlargement = entryRect.calculateAreaEnlargement(newRect);
                    area = entryRect.calculateArea();
                    if (childrenAreLeaves) {
                        cost = this.calculateOverlapEnlargement(entries, entry, newRect);
                    } else {
                        cost = areaEnlargement;
                    }
                } while(!(cost < minCost) && (cost != minCost || !(areaEnlargement < minAreaEnlargement)) && (cost != minCost || areaEnlargement != minAreaEnlargement || !(area < minArea)));

                bestEntry = entry;
                minCost = cost;
                minAreaEnlargement = areaEnlargement;
                minArea = area;
            }
        }

        return current;
    }

    private double calculateOverlapEnlargement(List<Entry> entries, Entry entry, Rectangle newRect) {
        Rectangle enlarged = entry.getRectangle().enlarge(newRect);
        double totalOverlapEnlargement = 0.0;
        Iterator var7 = entries.iterator();

        while(var7.hasNext()) {
            Entry other = (Entry)var7.next();
            if (other != entry) {
                Rectangle otherRect = other.getRectangle();
                double originalOverlap = entry.getRectangle().calculateOverlap(otherRect);
                double newOverlap = enlarged.calculateOverlap(otherRect);
                totalOverlapEnlargement += newOverlap - originalOverlap;
            }
        }

        return totalOverlapEnlargement;
    }

    private void split(Node node, List<Entry> allEntries) {
        System.out.println("split");
        node.setIsPartOfTheTree(false);
        IndexFileHandler.updateNode(node, IndexFileHandler.getTreeHeight());
        int splitAxis = this.chooseSplitAxis(allEntries);
        List<Entry> sortedEntries = this.sortEntriesAlongAxis(allEntries, splitAxis, true);
        int splitIndex = this.chooseSplitIndex(sortedEntries, splitAxis);
        List<Entry> group1 = new ArrayList(sortedEntries.subList(0, splitIndex));
        List<Entry> group2 = new ArrayList(sortedEntries.subList(splitIndex, sortedEntries.size()));
        Node newNode1 = this.createNewNode(node.isLeaf(), node.getParentNodeId());
        Node newNode2 = this.createNewNode(node.isLeaf(), node.getParentNodeId());
        newNode1.getEntries().addAll(group1);
        newNode2.getEntries().addAll(group2);
        IndexFileHandler.updateNode(newNode1, IndexFileHandler.getTreeHeight());
        IndexFileHandler.updateNode(newNode2, IndexFileHandler.getTreeHeight());
        Node newRoot = null;
        if (node.getBlockId() == IndexFileHandler.getRootBlockId()) {
            newRoot = this.createNewNode(false, -1L);
            newRoot.addEntry(this.createEntryForNode(newNode1));
            newRoot.addEntry(this.createEntryForNode(newNode2));
            newNode1.setParentNodeId(newRoot.getBlockId());
            newNode2.setParentNodeId(newRoot.getBlockId());
            IndexFileHandler.setRootBlockId(newRoot.getBlockId());
            IndexFileHandler.setTreeHeight(IndexFileHandler.getTreeHeight() + 1L);
            IndexFileHandler.updateMetaDataBlock();
            this.currentLevel = (int)IndexFileHandler.getTreeHeight() - 1;
        } else {
            Node parent = IndexFileHandler.readNode(node.getParentNodeId());
            parent.getEntries().removeIf((e) -> {
                return e.getBlockId() == node.getBlockId();
            });
            parent.addEntry(this.createEntryForNode(newNode1));
            parent.addEntry(this.createEntryForNode(newNode2));
            if (parent.getEntries().size() > this.maxEntries) {
                this.overflowTreatment(parent, new ArrayList(parent.getEntries()), this.getCurrentLevel(parent));
            }

            IndexFileHandler.updateNode(parent, IndexFileHandler.getTreeHeight());
        }

        this.updateChildParentReferences(newNode1);
        this.updateChildParentReferences(newNode2);
        IndexFileHandler.updateNode(newNode1, IndexFileHandler.getTreeHeight());
        IndexFileHandler.updateNode(newNode2, IndexFileHandler.getTreeHeight());
        if (newRoot != null) {
            IndexFileHandler.updateNode(newRoot, IndexFileHandler.getTreeHeight());
        }

    }

    private void updateChildParentReferences(Node parentNode) {
        if (!parentNode.isLeaf()) {
            Iterator var2 = parentNode.getEntries().iterator();

            while(var2.hasNext()) {
                Entry entry = (Entry)var2.next();

                try {
                    Node child = IndexFileHandler.readNode(entry.getBlockId());
                    child.setParentNodeId(parentNode.getBlockId());
                    IndexFileHandler.updateNode(child, IndexFileHandler.getTreeHeight());
                } catch (Exception var5) {
                    System.out.println("Couldn't read node with block id " + entry.getBlockId() + ", line 283");
                    return;
                }
            }

        }
    }

    private int chooseSplitAxis(List<Entry> entries) {
        int dimensions = ((Entry)entries.get(0)).getRectangle().getMin().length;
        double minS = Double.POSITIVE_INFINITY;
        int bestAxis = 0;

        for(int axis = 0; axis < dimensions; ++axis) {
            double S = this.computeMarginSum(entries, axis, true) + this.computeMarginSum(entries, axis, false);
            if (S < minS) {
                minS = S;
                bestAxis = axis;
            }
        }

        return bestAxis;
    }

    private double computeMarginSum(List<Entry> entries, int axis, boolean useLower) {
        List<Entry> sorted = this.sortEntriesAlongAxis(entries, axis, useLower);
        double sum = 0.0;
        int size = sorted.size();

        for(int k = this.minEntries - 1; k < size - this.minEntries; ++k) {
            int splitIndex = k + 1;
            List<Entry> group1 = sorted.subList(0, splitIndex);
            List<Entry> group2 = sorted.subList(splitIndex, size);
            Rectangle mbr1 = this.computeMBR(group1);
            Rectangle mbr2 = this.computeMBR(group2);
            sum += mbr1.calculateMargin() + mbr2.calculateMargin();
        }

        return sum;
    }

    private int chooseSplitIndex(List<Entry> sortedEntries, int axis) {
        int bestSplitIndex = this.minEntries;
        double minOverlap = Double.POSITIVE_INFINITY;
        double minArea = Double.POSITIVE_INFINITY;
        int size = sortedEntries.size();

        for(int k = this.minEntries - 1; k < size - this.minEntries; ++k) {
            int splitIndex = k + 1;
            List<Entry> group1 = sortedEntries.subList(0, splitIndex);
            List<Entry> group2 = sortedEntries.subList(splitIndex, size);
            Rectangle mbr1 = this.computeMBR(group1);
            Rectangle mbr2 = this.computeMBR(group2);
            double overlap = mbr1.calculateOverlap(mbr2);
            double area = mbr1.calculateArea() + mbr2.calculateArea();
            if (overlap < minOverlap || overlap == minOverlap && area < minArea) {
                minOverlap = overlap;
                minArea = area;
                bestSplitIndex = splitIndex;
            }
        }

        return bestSplitIndex;
    }

    private Rectangle computeMBR(List<Entry> entries) {
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("Empty entries");
        } else {
            double[] min = (double[])((Entry)entries.get(0)).getRectangle().getMin().clone();
            double[] max = (double[])((Entry)entries.get(0)).getRectangle().getMax().clone();
            int dimensions = min.length;
            Iterator var5 = entries.iterator();

            while(var5.hasNext()) {
                Entry entry = (Entry)var5.next();
                Rectangle rect = entry.getRectangle();

                for(int i = 0; i < dimensions; ++i) {
                    min[i] = Math.min(min[i], rect.getMin()[i]);
                    max[i] = Math.max(max[i], rect.getMax()[i]);
                }
            }

            return new Rectangle(min, max);
        }
    }

    private List<Entry> sortEntriesAlongAxis(List<Entry> entries, int axis, boolean useLower) {
        List<Entry> sorted = new ArrayList(entries);
        sorted.sort(Comparator.comparingDouble((e) -> {
            return useLower ? e.getRectangle().getMin()[axis] : e.getRectangle().getMax()[axis];
        }));
        return sorted;
    }

    private Node createNewNode(boolean isLeaf, long parentNodeId) {
        long blockId = (long)(this.nextBlockId++);
        Node node = new Node(new ArrayList(), isLeaf, blockId, parentNodeId);
        IndexFileHandler.createNode(node);
        return node;
    }

    private Entry createEntryForNode(Node node) {
        Rectangle mbr = this.computeMBR(node.getEntries());
        Entry entry = new Entry(mbr, node.getBlockId(), node.isLeaf());
        return entry;
    }
}
