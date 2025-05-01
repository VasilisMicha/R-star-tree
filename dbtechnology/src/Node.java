import java.io.Serializable;

class Node implements Serializable {
    private static final int NODE_SIZE = 32 * 1024;
    private static final int MAX_ENTRIES; //calculate max entries
    private static final int MIN_ENTRIES; //40% of max
    private int level;

}