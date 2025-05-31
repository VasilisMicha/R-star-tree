import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IndexFileHandler {

    private static final int BLOCK_SIZE = 32 * 1024; // 32 KB = 32 * 1024 bytes
    private static final String PATH_TO_FILE = "src//indexfile.dat";
    private static long rootBlockId;
    private static long dataDimensions;
    private static long treeHeight;

    public static synchronized void setTreeHeight(long height) {
        treeHeight = height;
        updateMetaDataBlock();
    }

    public static long getTreeHeight() {
        return treeHeight;
    }

    // Modified to handle root updates atomically
    public static synchronized void setRootBlockId(long id) {
        rootBlockId = id;
        updateMetaDataBlock(); // Persist immediately
    }

    public static long getRootBlockId(){
        return rootBlockId;
    }

    // Calculates the total blocks in the indexFile
    // Reads the data from the CSV files and adds it to the indexFile
    static void initializeIndexFile(int dimensions, boolean makeNewIndexFile) {
        try {
            // Checks if an indexFile already exists, initialise the metaData from the metadata block (block 0 of the file)
            if (!makeNewIndexFile && Files.exists(Paths.get(PATH_TO_FILE))) {
                ArrayList<Long> metadata = readMetaDataBlock();
                if (metadata != null && metadata.size() >= 3) {
                    dataDimensions = metadata.get(0);
                    rootBlockId = metadata.get(1); // Read saved root ID
                    treeHeight = metadata.get(2);
                }
            }
            // Else if not exists or a new one is to be made, initialize a new indexFile
            else {
                Files.deleteIfExists(Paths.get(PATH_TO_FILE)); // Resetting/Deleting index file data
                dataDimensions = dimensions;
                if (dataDimensions <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                rootBlockId = -1;
                treeHeight = 0;
                updateMetaDataBlock();
            }
        } catch(Exception e){e.printStackTrace();}
    }

    // Reads the data from the given file path
    // Reads the Block 0, which is used for the metadata
    // Returns an ArrayList<Integer> which includes the values for the metadata in the given file
    // Updates the metadata on the given file path
    // Saves the current dataDimensions, the block size and the new total blocks on the block 0 of the file

    public static ArrayList<Long> readMetaDataBlock() {
        try (RandomAccessFile raf = new RandomAccessFile(PATH_TO_FILE, "rw")) {
            byte[] block = new byte[BLOCK_SIZE];
            raf.seek(0);
            raf.readFully(block);

            // Read metadata length (first 4 bytes)
            byte[] lengthBytes = new byte[4];
            System.arraycopy(block, 0, lengthBytes, 0, 4);
            int metaDataLength = (int) DataFileHandler.deserialize(lengthBytes);

            // Read metadata
            byte[] metaDataBytes = new byte[metaDataLength];
            System.arraycopy(block, 4, metaDataBytes, 0, metaDataLength);
            return (ArrayList<Long>) DataFileHandler.deserialize(metaDataBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read metadata", e);
        }
    }

    public static void updateMetaDataBlock() {
        try {
            ArrayList<Long> dataFileMetaData = new ArrayList<>();
            dataFileMetaData.add(dataDimensions);
            dataFileMetaData.add(rootBlockId);
            dataFileMetaData.add(treeHeight);
            byte[] metaDataInBytes = DataFileHandler.serialize(dataFileMetaData);
            byte[] goodPutLengthInBytes = DataFileHandler.serialize(metaDataInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(metaDataInBytes, 0, block, goodPutLengthInBytes.length, metaDataInBytes.length);

            RandomAccessFile f = new RandomAccessFile(new File(PATH_TO_FILE), "rw");
            f.write(block);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createNode(Node node) {
        try {
            byte[] nodeInBytes = DataFileHandler.serialize(node);
            byte[] goodPutLengthInBytes = DataFileHandler.serialize(nodeInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(nodeInBytes, 0, block, goodPutLengthInBytes.length, nodeInBytes.length);
            System.out.println("Create: blockId: "+node.getBlockId()+" parentId: "+node.getParentNodeId()+" entries: "+node.getEntries().size());
            FileOutputStream fos = new FileOutputStream(PATH_TO_FILE,true);
            BufferedOutputStream bout = new BufferedOutputStream(fos);
            bout.write(block);
            updateMetaDataBlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Node readNode(long blockId) {
        try (RandomAccessFile raf = new RandomAccessFile(PATH_TO_FILE, "rws")) {
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);
            raf.seek(blockId*BLOCK_SIZE);
            byte[] node = new byte[BLOCK_SIZE];
            int size = bis.read(node,0,BLOCK_SIZE);
            if (size != BLOCK_SIZE)
                throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");
            byte[] sizeOfData = DataFileHandler.serialize(new Random().nextInt()); // the amount of data in bytes stored in the node
            System.arraycopy(node, 0, sizeOfData, 0, sizeOfData.length);
            byte[] nodeInBytes = new byte[(Integer)DataFileHandler.deserialize(sizeOfData)];
            System.arraycopy(node, sizeOfData.length, nodeInBytes, 0, nodeInBytes.length);

            return (Node)DataFileHandler.deserialize(nodeInBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Updates the indexFile block with the corresponding given already saved Node
    // In case node's block id is the root's and the given parameter totalLevelsOfTreeIndex is changed during the tree's changes then
    // the totalLevelsOfTreeIndex variable's value is increased by one
    public static void updateNode(Node node, long totalLevelsOfTreeIndex) {
        if (node.getParentNodeId() != -1) {
            Node parent = readNode(node.getParentNodeId());
            if (!parent.getIsPartOfTheTree()) return;
        }

        try {
            byte[] nodeInBytes = DataFileHandler.serialize(node);
            byte[] goodPutLengthInBytes = DataFileHandler.serialize(nodeInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.out.println("Update: blockId: "+node.getBlockId()+" parentId: "+node.getParentNodeId()+" entries: "+node.getEntries().size());
            System.arraycopy(nodeInBytes, 0, block, goodPutLengthInBytes.length, nodeInBytes.length);

            RandomAccessFile f = new RandomAccessFile(new File(PATH_TO_FILE), "rw");
            f.seek(node.getBlockId()*BLOCK_SIZE); // this basically reads n bytes in the file
            f.write(block);
            f.close();

            if (node.getBlockId() == rootBlockId && treeHeight != totalLevelsOfTreeIndex)
                updateLevelsOfTreeInIndexFile();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Updates the metadata block with an increased level of tree index in the indexFile
    // Saves the current dataDimensions, the block size, the total blocks on the block 0 and the new increased totalLevelsOfTreeIndex
    private static void updateLevelsOfTreeInIndexFile()
    {
        try {
            ArrayList<Long> dataFileMetaData = new ArrayList<>();
            dataFileMetaData.add(dataDimensions);
            dataFileMetaData.add(++treeHeight);
            byte[] metaDataInBytes = DataFileHandler.serialize(dataFileMetaData);
            byte[] goodPutLengthInBytes = DataFileHandler.serialize(metaDataInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(metaDataInBytes, 0, block, goodPutLengthInBytes.length, metaDataInBytes.length);

            RandomAccessFile f = new RandomAccessFile(new File(PATH_TO_FILE), "rw");
            f.write(block);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Calculates and return an integer which represents the maximum number of records a block of BLOCK_SIZE can have
    public static int calculateMaxEntriesInNode() {
        ArrayList<Entry> entries = new ArrayList<>();
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            double[] min = new double[(int) dataDimensions];
            double[] max = new double[(int) dataDimensions];
            for (int d = 0; d < dataDimensions; d++){
                min[d] = 0.0;
                max[d] = 0.0;
            }
            Entry entry = new Entry(new Rectangle(min, max), 0,  false);
            entries.add(entry);
            byte[] nodeInBytes = new byte[0];
            byte[] goodPutBytes = new byte[0];
            try {
                nodeInBytes = DataFileHandler.serialize(new Node(entries, false, 0, 0));
                goodPutBytes = DataFileHandler.serialize(nodeInBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (goodPutBytes.length + nodeInBytes.length > BLOCK_SIZE)
                break;
        }
        return i;
    }
    public static long getMaxBlockId() {
        File file = new File(PATH_TO_FILE);
        if (!file.exists()) return 0;
        return (file.length() / BLOCK_SIZE) - 1; // Block 0 is metadata
    }
}