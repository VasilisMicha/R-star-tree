import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class TestRStarTree {

    // Test node splitting
    @Test
    void testSplit() {
        IndexFileHandler.initializeIndexFile(3, true);
        RStarTree tree = new RStarTree();
        for (int i=0; i<400; i++) {
            tree.insert(new Point(new double[]{i,i}));
        }
        assertEquals(IndexFileHandler.getTreeHeight(), 2);
    }

}
