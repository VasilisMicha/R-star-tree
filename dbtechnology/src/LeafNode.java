/**
 * Class that represents a leaf node of R*-Tree.
 * Contains the data entries (LeafEntry) and maintains the min covering rectangle (MBR).
 * Supports point insertion, MBR update, and node splitting when the max number of entries is exceeded.
 * @author athinaanv
 **/


import java.util.ArrayList;
import java.util.List;

public class LeafNode extends Node {
    private List<LeafEntry> entries;

    //Constructor using dimensions
    public LeafNode(int dimensions) {
        this.level = 0;
        this.entries = new ArrayList<>();
        this.mbr = new Rectangle(
                new double[dimensions],  //min (initialized to 0)
                new double[dimensions]   //max (initialized to 0)
        );
    }

    public List<LeafEntry> getEntries() {
        return entries;
    }

    public Node addEntry(LeafEntry entry) {
        entries.add(entry);
        updateMBR(entry.getPoint());

        if (entries.size() > RStarTree.MAX_ENTRIES) {
            return split();
        }
        return null;
    }

    private void updateMBR(Point point) {
        double[] coords = point.getCoordinates();
        if (mbr == null) {
            mbr = new Rectangle(coords.clone(), coords.clone());
        } else {
            for (int i = 0; i < coords.length; i++) {
                mbr.getMin()[i] = Math.min(mbr.getMin()[i], coords[i]);
                mbr.getMax()[i] = Math.max(mbr.getMax()[i], coords[i]);
            }
        }
    }

/*************************************
    To-Do: Split based on:
    -> Minimum overlap
    -> Minimum total area
    -> Load balancing
*************************************/

    private Node split() {
        int mid = entries.size() / 2;
        LeafNode sibling = new LeafNode(mbr.getMin().length);

        List<LeafEntry> toMove = new ArrayList<>(entries.subList(mid, entries.size()));
        entries = new ArrayList<>(entries.subList(0, mid));

        for (LeafEntry entry : toMove) {
            sibling.entries.add(entry);
            sibling.updateMBR(entry.getPoint());
        }

        this.mbr = null;
        for (LeafEntry entry : entries) {
            updateMBR(entry.getPoint());
        }

        return sibling;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
}