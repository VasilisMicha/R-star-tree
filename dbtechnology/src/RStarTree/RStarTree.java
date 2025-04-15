import javax.swing.text.AbstractDocument;
import java.io.RandomAccessFile;

public class RStarTree {
    private static final ROOT_BLOCK_ID = 0;

    public RStarTree(){
        root = null;
    }

    private void createRoot() {
        RandomAccessFile raf = new RandomAccessFile("indexfile.txt","rw");
        LeafEntry entry = new LeafEntry();
    }

    private void chooseSubtree(){

    }
}
