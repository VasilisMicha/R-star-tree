/**
 * Abstract class representing a node of an R*-Tree (internal/leaf).
 * It contains information about the node's level and the min covering rectangle (MBR).
 * Subclasses define whether they are leaves or internal nodes.
 * @author VasilisMicha, athinaanv
 **/

import java.io.Serializable;

abstract class Node implements Serializable {
    protected int level;  //0=leaf
    protected Rectangle mbr; //Min bounding rectangle

    public int getLevel() {
        return level;
    }

    public Rectangle getMBR() {
        return mbr;
    }

    public abstract boolean isLeaf();
}