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
    static final String PathToDatafile = "src//datafile.dat";
    public static String PathToCsvFile = "src//data2.csv";
    private static final String DELIMITER = ",";
    public static int dataDimensions;
    private static int maxRecords;

    public static String getDelimiter(){ return DELIMITER; }

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


    // Calculates the total blocks in the datafile
    // Reads the data from the CSV files and adds it to the datafile
    static void initializeDataFile(int dimensions, boolean makeNewDataFile){
        try{
            // Checks if a datafile already exists, initialise the metaData from the metadata block (block 0 of the file)
            // If already exists, initialise the variables with the values of the dimensions, block size and total blocks of the data file
            if (!makeNewDataFile && Files.exists(Paths.get(PathToDatafile)))
            {
                ArrayList<Integer> dataFileMetaData = readMetaDataBlock();
                if (dataFileMetaData == null)
                    throw new IllegalStateException("Could not read datafile's Meta Data Block properly");
                dataDimensions = dataFileMetaData.get(0);
                if (dataDimensions  <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                if (dataFileMetaData.get(1) != BLOCK_SIZE)
                    throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");
                maxRecords = dataFileMetaData.get(2);
            }
            // Else initialize a new datafile
            else {
                Files.deleteIfExists(Paths.get(PathToDatafile)); // Resetting/Deleting dataFile data
                dataDimensions = dimensions;
                if (dataDimensions <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                maxRecords = calculateMaxPointsInBlock();
                createMetaDataBlock();
                ArrayList<Point> points = new ArrayList<>();
                BufferedReader csvReader = (new BufferedReader(new FileReader(PathToCsvFile)));
                String stringPoint;
                while ((stringPoint = csvReader.readLine()) != null)
                {
                    if (points.size() == maxRecords)
                    {
                        writeDataFileBlock(points);
                        points =  new ArrayList<>();
                    }
                    Point point = new Point(stringPoint);
                    point.setBlockId(getMaxBlockId() + 1);
                    points.add(point);
                }
                if (!points.isEmpty()){
                    writeDataFileBlock(points);
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }


    // Reads the data from the given file path
    // Reads the Block 0, which is used for the metadata
    // Returns an ArrayList<Integer> which includes the values for the metadata in the given file
    private static ArrayList<Integer> readMetaDataBlock(){
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(PathToDatafile), "rw");
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] block = new byte[BLOCK_SIZE];
            if (bis.read(block,0,BLOCK_SIZE) != BLOCK_SIZE)
                throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");

            byte[] goodPutLengthInBytes = serialize(new Random().nextInt()); // Serializing an integer in order to get the size of goodPutLength in bytes
            System.arraycopy(block, 0, goodPutLengthInBytes, 0, goodPutLengthInBytes.length);

            byte[] dataInBlock = new byte[(Integer)deserialize(goodPutLengthInBytes)];
            System.arraycopy(block, goodPutLengthInBytes.length, dataInBlock, 0, dataInBlock.length);

            return (ArrayList<Integer>)deserialize(dataInBlock);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static void createMetaDataBlock() {
        try {
            ArrayList<Integer> dataFileMetaData = new ArrayList<>();
            dataFileMetaData.add(dataDimensions);
            dataFileMetaData.add(BLOCK_SIZE);
            dataFileMetaData.add(maxRecords);
            byte[] metaDataInBytes = serialize(dataFileMetaData);
            byte[] goodPutLengthInBytes = serialize(metaDataInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(metaDataInBytes, 0, block, goodPutLengthInBytes.length, metaDataInBytes.length);

            RandomAccessFile f = new RandomAccessFile(new File(PathToDatafile), "rw");
            f.write(block);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Reads a block from datafile based on the id
     * @param blockId
     * @return
     */
    static ArrayList<Point> readDataFileBlock(long blockId){
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int calculateMaxPointsInBlock(){
        ArrayList<Point> points = new ArrayList<>();
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            double[] coords = new double[(int) dataDimensions];
            for (int d = 0; d < dataDimensions; d++){
                coords[d] = 0.0;
            }
            Point point = new Point(coords);
            points.add(point);
            byte[] blockInBytes = new byte[0];
            byte[] goodPutBytes = new byte[0];
            try {
                blockInBytes = DataFileHandler.serialize(points);
                goodPutBytes = DataFileHandler.serialize(blockInBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (goodPutBytes.length + blockInBytes.length > BLOCK_SIZE)
                break;
        }
        return i;
    }

    public static long getMaxBlockId() {
        File file = new File(PathToDatafile);
        if (!file.exists()) return 0;
        return (file.length() / BLOCK_SIZE) - 1; // Block 0 is metadata
    }
}