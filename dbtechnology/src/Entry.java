/**
 * This class represents the structural element that connects the nodes of the tree and carries
 * information about the spatial boundaries of the subtrees.
 * @author vasilisMicha, athinaanv
 **/

import java.io.Serializable;

class Entry implements Serializable {
    private Rectangle rectangle;
    private Node child; // direct reference

    public Entry(Rectangle rectangle, Node child) {
        this.rectangle = rectangle;
        this.child = child;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public Node getChild() {
        return child;
    }
}