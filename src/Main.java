import java.io.File;

public class Main {
    public static void main(String[] args) {
        IndexFileHandler.initializeIndexFile(3, true);
        System.out.println(IndexFileHandler.calculateMaxEntriesInNode());
    }
}