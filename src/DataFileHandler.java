
/**
 * Class for managing the data file.
 * Includes methods for saving, updating, reading data,
 * as well as handling metadata (block size, etc.).
 **/

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

class DataFileHandler {
  private static final int BLOCK_SIZE = 32 * 1024; // Block size in bytes (32KB)
  static final String PathToDatafile = "src//datafile.dat"; // Path to "data file"
  public static String PathToCsvFile = "src//data2.csv"; // Path to CSV file
  private static final String DELIMITER = ","; // CSV delimiter
  public static int dataDimensions; // Data dimensionality
  private static int maxRecords; // Max number of records per block

  public static String getDelimiter() {
    return DELIMITER;
  }

  /**
   * Serializes an object to a byte array.
   * 
   * @param obj The object to serialize.
   * @return Byte array.
   * @throws IOException
   **/
  public static byte[] serialize(Object obj) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
    objectOutputStream.writeObject(obj);
    return out.toByteArray();
  }

  /**
   * Deserializes a byte array back to an object.
   * 
   * @param data Byte array.
   * @return Object.
   * @throws IOException
   * @throws ClassNotFoundException
   **/
  public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
    ByteArrayInputStream in = new ByteArrayInputStream(data);
    ObjectInputStream objectInputStream = new ObjectInputStream(in);
    return objectInputStream.readObject();
  }

  /**
   * Initializes the data file.
   * 
   * @param dimensions      Number of data dimensions.
   * @param makeNewDataFile Flag to create a new file.
   **/
  static void initializeDataFile(int dimensions, boolean makeNewDataFile) {
    try {
      if (!makeNewDataFile && Files.exists(Paths.get(PathToDatafile))) {
        ArrayList<Integer> dataFileMetaData = readMetaDataBlock();
        if (dataFileMetaData == null)
          throw new IllegalStateException("Failed to read metadata from the file.");
        dataDimensions = dataFileMetaData.get(0);
        if (dataDimensions <= 0)
          throw new IllegalStateException("Data dimensions must be a positive integer.");
        if (dataFileMetaData.get(1) != BLOCK_SIZE)
          throw new IllegalStateException("Block size mismatch: expected " + BLOCK_SIZE);
        maxRecords = dataFileMetaData.get(2);
      } else {
        Files.deleteIfExists(Paths.get(PathToDatafile));
        dataDimensions = dimensions;
        if (dataDimensions <= 0)
          throw new IllegalStateException("Data dimensions must be a positive integer.");
        maxRecords = calculateMaxPointsInBlock(); // Calculates how many points fit in a block
        createMetaDataBlock(); // Create metadata block

        ArrayList<Point> points = new ArrayList<>();
        try (BufferedReader csvReader = new BufferedReader(new FileReader(PathToCsvFile))) {
          String stringPoint;
          while ((stringPoint = csvReader.readLine()) != null) {
            if (points.size() == maxRecords) {
              writeDataFileBlock(points);
              points = new ArrayList<>();
            }
            Point point = new Point(stringPoint);
            point.setBlockId(getMaxBlockId() + 1);
            points.add(point);
          }
          if (!points.isEmpty()) {
            writeDataFileBlock(points);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Reads the metadata block (block 0) from the data file.
   * 
   * @return A list of integers containing the metadata.
   */
  private static ArrayList<Integer> readMetaDataBlock() {
    File file = new File(PathToDatafile);
    try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileInputStream fis = new FileInputStream(raf.getFD());
        BufferedInputStream bis = new BufferedInputStream(fis)) {
      byte[] block = new byte[BLOCK_SIZE];
      if (bis.read(block, 0, BLOCK_SIZE) != BLOCK_SIZE)
        throw new IllegalStateException("Full block of " + BLOCK_SIZE + " bytes was not read.");

      byte[] goodPutLengthInBytes = serialize(new Random().nextInt());
      System.arraycopy(block, 0, goodPutLengthInBytes, 0, goodPutLengthInBytes.length);

      byte[] dataInBlock = new byte[(Integer) deserialize(goodPutLengthInBytes)];
      System.arraycopy(block, goodPutLengthInBytes.length, dataInBlock, 0, dataInBlock.length);

      return (ArrayList<Integer>) deserialize(dataInBlock);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Creates and writes the metadata block to the file.
   **/
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
   * Reads a specific block from the data file by blockId.
   * 
   * @param blockId Block number.
   * @return List of points (Point) contained in the block.
   **/
  static ArrayList<Point> readDataFileBlock(long blockId) {
    File file = new File(PathToDatafile);
    try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileInputStream fis = new FileInputStream(raf.getFD());
        BufferedInputStream bis = new BufferedInputStream(fis)) {

      raf.seek(blockId * BLOCK_SIZE); // Move to the correct block
      byte[] block = new byte[BLOCK_SIZE];
      if (bis.read(block, 0, BLOCK_SIZE) != BLOCK_SIZE)
        throw new IllegalStateException("Full block of " + BLOCK_SIZE + " bytes was not read.");

      byte[] goodPutLengthInBytes = serialize(new Random().nextInt());
      System.arraycopy(block, 0, goodPutLengthInBytes, 0, goodPutLengthInBytes.length);

      byte[] recordsInBlock = new byte[(Integer) deserialize(goodPutLengthInBytes)];
      System.arraycopy(block, goodPutLengthInBytes.length, recordsInBlock, 0, recordsInBlock.length);

      return (ArrayList<Point>) deserialize(recordsInBlock);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Writes a list of points to a data file block.
   * 
   * @param records List of points to write.
   **/
  private static void writeDataFileBlock(ArrayList<Point> records) {
    try {
      byte[] recordInBytes = serialize(records);
      byte[] goodPutLengthInBytes = serialize(recordInBytes.length);

      byte[] block = new byte[BLOCK_SIZE];
      System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
      System.arraycopy(recordInBytes, 0, block, goodPutLengthInBytes.length, recordInBytes.length);

      FileOutputStream fos = new FileOutputStream(PathToDatafile, true);
      BufferedOutputStream bout = new BufferedOutputStream(fos);
      bout.write(block);
      bout.flush();
      bout.close();
      fos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Calculates the maximum number of points that can fit into a block,
   * given the data dimensions and block size.
   * 
   * @return Max number of points per block.
   **/
  public static int calculateMaxPointsInBlock() {
    ArrayList<Point> points = new ArrayList<>();
    int i;
    for (i = 0; i < Integer.MAX_VALUE; i++) {
      double[] coords = new double[dataDimensions];
      for (int d = 0; d < dataDimensions; d++) {
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
    return i - 100;
  }

  /**
   * Returns the highest block ID present in the file.
   * 
   * @return The maximum blockId.
   **/
  public static long getMaxBlockId() {
    File file = new File(PathToDatafile);
    if (!file.exists())
      return 0;
    return (file.length() / BLOCK_SIZE) - 1; // Block 0 is metadata
  }
}
