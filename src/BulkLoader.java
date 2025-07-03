import java.io.*;
import java.util.*;

public class BulkLoader {
    private final int maxEntries;
    private int nextBlockId = 1;

    public BulkLoader(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public void bulkLoadFromDataFileBlocks() throws IOException {
        long maxBlockId = DataFileHandler.getMaxBlockId();
        List<Node> leafNodes = new ArrayList<>();
        int dimensions = DataFileHandler.getDataDimensions();
        IndexFileHandler.initializeIndexFile(dimensions, true);

        for (int blockId = 1; blockId < maxBlockId; blockId++) {
            List<Point> blockPoints = DataFileHandler.readDataFileBlock(blockId);
            if (blockPoints == null || blockPoints.isEmpty()) continue;

            List<Entry> entries = new ArrayList<>();
            for (Point p : blockPoints) {
                entries.add(new Entry(p.toRectangle(), p.getBlockId(), true));
            }

            for (int i = 0; i < entries.size(); i += maxEntries) {
                int end = Math.min(i + maxEntries, entries.size());
                Node node = createNewNode(true, -1);
                for (int j = i; j < end; j++) {
                    node.addEntry(entries.get(j));
                }
                IndexFileHandler.updateNode(node, 1);
                leafNodes.add(node);
            }
        }

        buildUpperLevels(leafNodes, dimensions);
    }

    private void buildUpperLevels(List<Node> currentLevel, int dimensions) {
        int height = 1;
        while (currentLevel.size() > 1) {
            List<Point> centers = new ArrayList<>();
            for (Node node : currentLevel) {
                centers.add(RStarTree.computeMBR(node.getEntries()).getCenter());
            }
            List<List<Node>> groups = spatialGroup(currentLevel, centers, dimensions);
            List<Node> nextLevel = new ArrayList<>();
            for (List<Node> group : groups) {
                ArrayList<Entry> entries = new ArrayList<>();
                Node parent = new Node(new ArrayList<>(), false, nextBlockId++);
                for (Node child : group) {
                    child.setParentNodeId(parent.getBlockId());
                    IndexFileHandler.updateNode(child, height);
                    Rectangle mbr = RStarTree.computeMBR(child.getEntries());
                    parent.addEntry(new Entry(mbr, child.getBlockId(), child.isLeaf()));
                }
                IndexFileHandler.updateNode(parent, height + 1);
                nextLevel.add(parent);
            }
            currentLevel = nextLevel;
            height++;
        }
        Node root = currentLevel.get(0);
        IndexFileHandler.setRootBlockId(root.getBlockId());
        IndexFileHandler.setTreeHeight(height);
        IndexFileHandler.updateMetaDataBlock();
    }

    private List<List<Node>> spatialGroup(List<Node> nodes, List<Point> centers, int dimensions) {
        int nodeCount = nodes.size();
        int L = (int) Math.ceil((double) nodeCount / maxEntries);
        int S = (int) Math.ceil(Math.pow(L, 1.0 / dimensions));
        return recursiveGroup(nodes, centers, 0, dimensions, S);
    }

    private List<List<Node>> recursiveGroup(List<Node> nodes, List<Point> centers, int dim, int totalDims, int S) {
        if (dim >= totalDims || nodes.size() <= maxEntries) {
            List<List<Node>> singleton = new ArrayList<>();
            singleton.add(nodes);
            return singleton;
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < centers.size(); i++) indices.add(i);
        indices.sort(Comparator.comparingDouble(i -> centers.get(i).getCoordinates()[dim]));

        List<List<Node>> result = new ArrayList<>();
        int sliceSize = (int) Math.ceil((double) indices.size() / S);
        for (int i = 0; i < S; i++) {
            int start = i * sliceSize;
            int end = Math.min(start + sliceSize, indices.size());
            if (start >= end) break;
            List<Node> subNodes = new ArrayList<>();
            List<Point> subCenters = new ArrayList<>();
            for (int j = start; j < end; j++) {
                subNodes.add(nodes.get(indices.get(j)));
                subCenters.add(centers.get(indices.get(j)));
            }
            result.addAll(recursiveGroup(subNodes, subCenters, dim + 1, totalDims, S));
        }
        return result;
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
}