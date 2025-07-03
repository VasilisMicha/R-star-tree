/**
 * This class manages a block-based index file used for storing a tree structure.
 * It handles metadata, node creation, reading, and updates within a fixed-size block file.
 * Block 0 is reserved for metadata (dimensions, root ID, tree height).
 * The file operates with 32KB blocks and supports serialization-based persistence.
 **/

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IndexFileHandler {

    private static final int BLOCK_SIZE = 32 * 1024;
    private static final String PATH_TO_FILE = "src//indexfile.dat";
    private static long rootBlockId;
    private static long dataDimensions;
    private static long treeHeight;

    //Sets the tree height and updates metadata
    public static synchronized void setTreeHeight(long height) {
        treeHeight = height;
        updateMetaDataBlock(); //Persist change to file
    }
    public static long getTreeHeight() {
        return treeHeight;
    }
    public static synchronized void setRootBlockId(long id) {
        rootBlockId = id;
        updateMetaDataBlock(); //Persist immediately
    }
    public static long getRootBlockId(){
        return rootBlockId;
    }

    public static long getDataDimensions(){ return dataDimensions; }

    /**
     * Initializes the index file.
     * - If makeNewIndexFile is false and a file exists: load metadata.
     * - Otherwise: create/reset index file and store initial metadata.
     **/
    static void initializeIndexFile(int dimensions, boolean makeNewIndexFile) {
        try {
            if (!makeNewIndexFile && Files.exists(Paths.get(PATH_TO_FILE))) {
                //Read existing metadata
                ArrayList<Long> metadata = readMetaDataBlock();
                if (metadata != null && metadata.size() >= 3) {
                    dataDimensions = metadata.get(0);
                    rootBlockId = metadata.get(1);
                    treeHeight = metadata.get(2);
                }
            } else {
                //Create a new index file
                Files.deleteIfExists(Paths.get(PATH_TO_FILE)); //Removes an old file
                dataDimensions = dimensions;
                if (dataDimensions <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                rootBlockId = -1;
                treeHeight = 0;
                updateMetaDataBlock(); //Save initial metadata
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Reads metadata from block 0 of the file.
     * Returns an ArrayList<Long> containing: dataDimensions, rootBlockId, treeHeight.
     **/
    public static ArrayList<Long> readMetaDataBlock() {
        try (RandomAccessFile raf = new RandomAccessFile(PATH_TO_FILE, "rw")) {
            byte[] block = new byte[BLOCK_SIZE];
            raf.seek(0); //Go to the start of file
            raf.readFully(block); //Read block 0

            //First 4 bytes: metadata length
            byte[] lengthBytes = new byte[4];
            System.arraycopy(block, 0, lengthBytes, 0, 4);
            int metaDataLength = (int) DataFileHandler.deserialize(lengthBytes);

            //Next: metadata content
            byte[] metaDataBytes = new byte[metaDataLength];
            System.arraycopy(block, 4, metaDataBytes, 0, metaDataLength);
            return (ArrayList<Long>) DataFileHandler.deserialize(metaDataBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read metadata", e);
        }
    }

     //Writes metadata (dataDimensions, rootBlockId, treeHeight) to block 0.
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
            f.write(block); //Overwrite block 0
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Appends a new node to the end of the index file.
    public static void createNode(Node node) {
        try {
            byte[] nodeInBytes = DataFileHandler.serialize(node);
            byte[] goodPutLengthInBytes = DataFileHandler.serialize(nodeInBytes.length);

            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(nodeInBytes, 0, block, goodPutLengthInBytes.length, nodeInBytes.length);

            System.out.println("Create: blockId: " + node.getBlockId() +
                    " parentId: " + node.getParentNodeId() +
                    " entries: " + node.getEntries().size());

            FileOutputStream fos = new FileOutputStream(PATH_TO_FILE, true);
            BufferedOutputStream bout = new BufferedOutputStream(fos);
            bout.write(block);
            bout.close();

            updateMetaDataBlock(); //Update metadata if needed
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Reads and returns a node from the file by its block ID.
    public static Node readNode(long blockId) {
        System.out.println(blockId);
        try (RandomAccessFile raf = new RandomAccessFile(PATH_TO_FILE, "rws")) {
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);

            //Seek to the block
            raf.seek(blockId * BLOCK_SIZE);
            byte[] node = new byte[BLOCK_SIZE];
            int size = bis.read(node, 0, BLOCK_SIZE);
            if (size != BLOCK_SIZE)
                throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");

            //Read length of serialized node
            byte[] sizeOfData = DataFileHandler.serialize(new Random().nextInt());
            System.arraycopy(node, 0, sizeOfData, 0, sizeOfData.length);

            int dataSize = (Integer) DataFileHandler.deserialize(sizeOfData);
            byte[] nodeInBytes = new byte[dataSize];
            System.arraycopy(node, sizeOfData.length, nodeInBytes, 0, dataSize);

            return (Node) DataFileHandler.deserialize(nodeInBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates an existing node's block in the file.
     * Updates tree height in metadata if this is the root node and height has changed.
     **/
    public static void updateNode(Node node, long totalLevelsOfTreeIndex) {
        if (node.getParentNodeId() != -1) {
            System.out.println("size: "+node.getEntries().size());
            Node parent = readNode(node.getParentNodeId());
            if (!parent.getIsPartOfTheTree()) return; //Don't update if parent is not part of the tree
        }

        try {
            byte[] nodeInBytes = DataFileHandler.serialize(node);
            byte[] goodPutLengthInBytes = DataFileHandler.serialize(nodeInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];

            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(nodeInBytes, 0, block, goodPutLengthInBytes.length, nodeInBytes.length);

            System.out.println("Update: blockId: " + node.getBlockId() +
                    " parentId: " + node.getParentNodeId() +
                    " entries: " + node.getEntries().size());

            RandomAccessFile f = new RandomAccessFile(new File(PATH_TO_FILE), "rw");
            f.seek(node.getBlockId() * BLOCK_SIZE);
            f.write(block);
            f.close();

            //Update tree height if necessary
            if (node.getBlockId() == rootBlockId && treeHeight != totalLevelsOfTreeIndex)
                updateLevelsOfTreeInIndexFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates metadata with increased tree height.
     * Called when root node height increases.
     **/
    private static void updateLevelsOfTreeInIndexFile() {
        try {
            ArrayList<Long> dataFileMetaData = new ArrayList<>();
            dataFileMetaData.add(dataDimensions);
            dataFileMetaData.add(++treeHeight); //Increase height

            byte[] metaDataInBytes = DataFileHandler.serialize(dataFileMetaData);
            byte[] goodPutLengthInBytes = DataFileHandler.serialize(metaDataInBytes.length);

            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(metaDataInBytes, 0, block, goodPutLengthInBytes.length, metaDataInBytes.length);

            RandomAccessFile f = new RandomAccessFile(new File(PATH_TO_FILE), "rw");
            f.write(block); //Overwrite metadata block
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Estimates how many entries can fit in one node (BLOCK_SIZE bytes).
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

            Entry entry = new Entry(new Rectangle(min, max), 0, false);
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
        return i-300;
    }

    /**
     * Returns the highest block ID in the index file.
     * Metadata block (block 0) is excluded from count.
     **/
    public static long getMaxBlockId() {
        File file = new File(PATH_TO_FILE);
        if (!file.exists()) return 0;
        return (file.length() / BLOCK_SIZE) - 1; //Skip metadata block
    }
}
