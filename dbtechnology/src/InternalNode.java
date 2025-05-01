/**
 * This class represents an internal, non-leaf node in a hierarchical spatial data structure.
 * Extends the base node class to store child entries and manage their spatial relationships.
 * @author athinaanv
**/

import java.util.ArrayList;
import java.util.List;

class InternalNode extends Node {
    private List<Entry> entries;

    public InternalNode(int level) {
        this.level = level;
        this.entries = new ArrayList<>();
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
        updateMBR(entry.getRectangle());
    }

    void updateMBR(Rectangle rect) {
        if (mbr == null) {
            //Create a new MBR using cloned coordinates
            mbr = new Rectangle(rect.getMin().clone(), rect.getMax().clone());
        } else {
            //Expand MBR to encompass the new rectangle
            for (int i = 0; i < mbr.getMin().length; i++) {
                mbr.getMin()[i] = Math.min(mbr.getMin()[i], rect.getMin()[i]);
                mbr.getMax()[i] = Math.max(mbr.getMax()[i], rect.getMax()[i]);
            }
        }
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}