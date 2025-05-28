import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private static int dataDimensions;
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
     *
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


}
