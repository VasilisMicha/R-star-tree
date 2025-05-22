public class Main {
    public static void main(String[] args) {
        IndexFileHandler.initializeIndexFile(2, true);
        System.out.println(IndexFileHandler.calculateMaxEntriesInNode());
    }
}