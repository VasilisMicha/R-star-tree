import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class TestRStarTree {
    // Test data insertion
//    @Test void testInsert() {
//        IndexFileHandler.initializeIndexFile(2, true);
//        RStarTree tree = new RStarTree();
//        tree.insert(new Point(new double[]{1,1}));
//        assertEquals(1, tree.getRoot().getEntries().size());
//    }

    // Test node splitting
    @Test
    void testSplit() {
        IndexFileHandler.initializeIndexFile(3, true);
        RStarTree tree = new RStarTree();
        for (int i=0; i<80; i++) {
            tree.insert(new Point(new double[]{i,i}));
        }
        assertEquals(IndexFileHandler.getTreeHeight(), 2);
    }

}
