import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class that is used for various tasks in the datafile and indexfile such as saving and updating
 * It also stores metadata information (file paths, block sizes and other)
 */
class DataFileHandler {
    private static final int BLOCK_SIZE = 32 * 1024; // Each Block is 32KB
    static final String PathToDatafile = "datafile.dat";
    private static final String PathToIndexfile = "indexfile.dat";
    private static final String PathToCSV = "data.csv";
    private static final String Delimiter = ","; // The delimiter for csv file
    public static int dataDimensions;
    private static int datafileBlockCounter;
    private static int indexfileBlockCounter;
    private static int rStarTreeHeight;

    static String getPathToCsv() {
        return PathToCSV;
    }

    static String getDelimiter() {
        return Delimiter;
    }

    static int getDataDimensions() {
        return dataDimensions;
    }

    static int getDatafileBlockCounter() {
        return datafileBlockCounter;
    }

    static int getIndexfileBlockCounter() {
        return indexfileBlockCounter;
    }

    static int getRStarTreeHeight() {
        return rStarTreeHeight;
    }

    /**
     * It is used for serializing an Object to byte array
     * @param obj an Object
     * @return a byte array
     * @throws IOException
     */
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
        objectOutputStream.writeObject(obj);
        return out.toByteArray();
    }

    /**
     * It is used to deserialize a byte array to an Object
     *
     * @param data a byte array
     * @return an Object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream objectInputStream = new ObjectInputStream(in);
        return objectInputStream.readObject();
    }
    /**
     * Reads a block from datafile based on the id
     * @param blockId
     * @return
     */
    static ArrayList<Point> readDataFileBlock(int blockId){
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(PathToDatafile), "rw");
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);
            raf.seek(blockId*BLOCK_SIZE);
            //bis = new BufferedInputStream(fis);
            byte[] block = new byte[BLOCK_SIZE];
            if (bis.read(block,0,BLOCK_SIZE) != BLOCK_SIZE)
                throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");
            byte[] goodPutLengthInBytes = serialize(new Random().nextInt());
            System.arraycopy(block, 0, goodPutLengthInBytes, 0, goodPutLengthInBytes.length);

            byte[] recordsInBlock = new byte[(Integer)deserialize(goodPutLengthInBytes)];
            System.arraycopy(block, goodPutLengthInBytes.length, recordsInBlock, 0, recordsInBlock.length);

            return (ArrayList<Point>)deserialize(recordsInBlock);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void writeDataFileBlock(ArrayList<Point> records) {
        try {
            byte[] recordInBytes = serialize(records);
            byte[] goodPutLengthInBytes = serialize(recordInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(recordInBytes, 0, block, goodPutLengthInBytes.length, recordInBytes.length);

            FileOutputStream fos = new FileOutputStream(PathToDatafile,true);
            BufferedOutputStream bout = new BufferedOutputStream(fos);
            bout.write(block);
            IndexFileHandler.updateMetaDataBlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void savePointsToDataFile(List<Point> points) {
        ArrayList<Point> currentBlock = new ArrayList<>();
        int pointsPerBlock = 100;
        for (Point p : points) {
            currentBlock.add(p);
            if (currentBlock.size() >= pointsPerBlock) {
                writeDataFileBlock(currentBlock);
                currentBlock.clear();
            }
        }
        if (!currentBlock.isEmpty()) {
            writeDataFileBlock(currentBlock);
        }
    }
}